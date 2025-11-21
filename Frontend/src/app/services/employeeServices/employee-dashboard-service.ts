import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginAuthentication } from '../login/login-authentication';

@Injectable({
  providedIn: 'root',
})
export class EmployeeDashboardService {
  private baseUrl = 'http://localhost:8080/api';
  private http = inject(HttpClient);
  private authService = inject(LoginAuthentication);

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
    });
  }

  // ✅ FIXED: Correct URL
  getEmployeeData(): Observable<any> {
    const empId = this.authService.getEmpIdFromToken();
    
    if (!empId) {
      throw new Error('Employee ID missing in token');
    }
    
    console.log('📡 Fetching employee data for empId:', empId);
    
    return this.http.get(`${this.baseUrl}/employees/${empId}/dashboard`, {
      headers: this.getHeaders(),
    });
  }

  // ✅ FIXED: Correct URL
  getSalarySlips(): Observable<any> {
    const empId = this.authService.getEmpIdFromToken();
    
    if (!empId) {
      throw new Error('Employee ID missing in token');
    }
    
    console.log('📡 Fetching salary slips for empId:', empId);
    
    return this.http.get(`${this.baseUrl}/salary-slip/employee/${empId}`, {
      headers: this.getHeaders(),
    });
  }

  getSalarySlipDetails(slipId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/salary-slip/${slipId}`, {
      headers: this.getHeaders(),
    });
  }

  downloadSalarySlip(slipId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/salary-slip/${slipId}/download`, {
      headers: this.getHeaders(),
      responseType: 'blob',
    });
  }

  raiseConcern(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/concerns`, formData, {
      headers: this.getHeaders(),
    });
  }

  // ✅ FIXED: Correct URL
  getEmployeeConcerns(): Observable<any> {
    const empId = this.authService.getEmpIdFromToken();
    
    if (!empId) {
      throw new Error('Employee ID missing in token');
    }
    
    console.log('📡 Fetching concerns for empId:', empId);
    
    return this.http.get(`${this.baseUrl}/concerns/employee/${empId}`, {
      headers: this.getHeaders(),
    });
  }

  getPendingConcernsCount(employeeId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/concerns/employee/${employeeId}/pending/count`, {
      headers: this.getHeaders(),
    });
  }
}
