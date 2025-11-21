import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { loginRequestDTO } from "../../LoginDTO's/loginRequestDTO";
import { catchError, Observable, tap, throwError, of } from 'rxjs';
import { map } from 'rxjs/operators'; // ✅ ADDED
import { loginAuthResponse } from "../../LoginDTO's/loginAuthResponse";
import { Router } from '@angular/router';
import { ChangePasswordDTO } from '../../components/change-password/change-password';

interface DecodedToken {
  sub: string; // User's email
  roles: string[];
  userId?: number; // User's unique ID
  orgId?: number;
  orgStatus?: 'PENDING' | 'APPROVED' | 'REJECTED';
  mustChangePassword?: boolean;
  exp: number;
}

@Injectable({
  providedIn: 'root',
})
export class LoginAuthentication {
  
  private http = inject(HttpClient);
  private router = inject(Router);

  // API endpoints are now clearly defined
  private apiUrl = 'http://localhost:8080/api/auth';
  private userApiUrl = 'http://localhost:8080/api/users';

  login(user: loginRequestDTO): Observable<loginAuthResponse> {
    return this.http.post<loginAuthResponse>(`${this.apiUrl}/login`, user).pipe(
      tap((response) => this.handleLoginSuccess(response)),
      catchError(this.handleError)
    );
  }

  private handleLoginSuccess(response: loginAuthResponse): void {
    if (!response.token) {
      console.error('No token received in login response');
      return;
    }

    localStorage.setItem('authToken', response.token);

    const payload = this.decodeToken();
    const role = payload?.roles?.[0] || null;
    const mustChange = payload?.mustChangePassword || false;

    // First, check if a password change is required
    if (mustChange) {
      this.router.navigate(['/change-password']);
      return;
    }

    switch (role) {
      case 'ROLE_BANK_ADMIN':
        this.router.navigate(['/bank-admin/dashboard']);
        break;
      case 'ROLE_ORGANIZATION':
        this.router.navigate(['/organization/dashboard']);
        break;
      case 'ROLE_EMPLOYEE':
        this.router.navigate(['/employee/dashboard']);
        break;
      case 'ROLE_ORG_ADMIN':
        this.router.navigate(['/organization/dashboard']);
        break;
      case 'ROLE_VENDOR':
        this.router.navigate(['/vendor/dashboard']);
        break;
      default:
        console.warn(`Unknown role '${role}', redirecting to home.`);
        this.router.navigate(['/']);
        break;
    }
  }

  changePassword(oldPassword: string, newPassword: string): Observable<string> {
    const userId = this.getUserIdFromToken();
    if (!userId) {
      return throwError(() => new Error('User not authenticated. Cannot change password.'));
    }

    const url = `${this.userApiUrl}/${userId}/change-password`;
    const payload: ChangePasswordDTO = { oldPassword, newPassword };

    return this.http.put(url, payload, { responseType: 'text' });
  }

  // --- TOKEN HELPER METHODS ---

  private decodeToken(): DecodedToken | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  logout(): void {
    localStorage.removeItem('authToken');
    sessionStorage.removeItem('auditLogsAuthenticated'); // ✅ Clear audit log auth
    this.router.navigate(['/login']);
  }

  getRoleFromToken(): string | null {
    return this.decodeToken()?.roles?.[0] || null;
  }

  getOrgIdFromToken(): number | null {
    return this.decodeToken()?.orgId || null;
  }

  getOrgStatusFromToken(): 'PENDING' | 'APPROVED' | 'REJECTED' | 'UNKNOWN' {
    return this.decodeToken()?.orgStatus || 'UNKNOWN';
  }

  private handleError(error: HttpErrorResponse) {
    const errorMessage = error.error?.message || 'Invalid credentials or server error.';
    console.error(`Backend returned code ${error.status}, body was: `, error.error);
    return throwError(() => new Error(errorMessage));
  }

  getEmpIdFromToken(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.empId || null;
    } catch (error) {
      console.error('Error parsing empId from token:', error);
      return null;
    }
  }

  getOrgNameFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.organizationName || payload.orgName || null;
    } catch (error) {
      console.error('Error parsing organization name from token:', error);
      return null;
    }
  }

  getUserIdFromToken(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId || payload.sub || null;
    } catch (error) {
      console.error('Error parsing user ID from token:', error);
      return null;
    }
  }

  getEmailFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || payload.email || null;
    } catch (error) {
      console.error('Error parsing email from token:', error);
      return null;
    }
  }

  getNameFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.name || payload.userName || null;
    } catch (error) {
      console.error('Error parsing name from token:', error);
      return null;
    }
  }

  /**
   * Step 1: Request OTP for password reset
   */
  requestPasswordReset(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }, { responseType: 'text' });
  }

  /**
   * Step 2: Verify OTP
   */
  verifyOTP(email: string, otp: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/verify-otp`, { email, otp }, { responseType: 'text' });
  }

  /**
   * Step 3: Reset Password with OTP
   */
  resetPasswordWithOTP(email: string, otp: string, newPassword: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, 
      { email, otp, newPassword }, 
      { responseType: 'text' }
    );
  }

  /**
   * ✅ NEW: Verify password for sensitive operations (audit logs)
   */
  verifyPassword(email: string, password: string): Observable<boolean> {
    console.log('🔐 ════════════════════════════════════════');
    console.log('🔐 FRONTEND: Verifying password');
    console.log('🔐 ════════════════════════════════════════');
    console.log('   Email:', email);
    console.log('   Endpoint:', `${this.apiUrl}/verify-password`);
    
    return this.http.post<any>(
      `${this.apiUrl}/verify-password`, // ✅ FIXED: Changed from baseUrl to apiUrl
      { email, password },
      { observe: 'response' }
    ).pipe(
      map(response => {
        console.log('✅ Password verification successful');
        console.log('   Status:', response.status);
        console.log('🔐 ════════════════════════════════════════\n');
        return response.status === 200;
      }),
      catchError(error => {
        console.error('❌ ════════════════════════════════════════');
        console.error('❌ FRONTEND: Password verification failed');
        console.error('❌ ════════════════════════════════════════');
        console.error('   Status:', error.status);
        console.error('   Error:', error.error);
        console.error('❌ ════════════════════════════════════════\n');
        return of(false);
      })
    );
  }
}
