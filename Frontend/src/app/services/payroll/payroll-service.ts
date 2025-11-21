import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface Employee {
  empId: number;
  empName: string;
  empEmail: string;
  phone?: string;
  departmentName?: string;
  departmentId?: number;
  salaryGradeId?: number;
  gradeCode?: string;

  salaryGrade?: {
    gradeId: number;
    gradeCode: string;
    basicSalary: number;
    hra: number;
    da: number;
    allowances: number;
    pf: number;
    organizationId?: number;
  };
}

export interface OrgAdmin {
  orgAdminId: number;
  name: string;
  email: string;
  salaryGrade?: {
    gradeCode?: string;
    basicSalary: number;
    hra: number;
    da: number;
    allowances: number;
    pf: number;
    salaryGradeId?: number;
  };
}

export interface SalaryDisbursalRequest {
  orgId: number;
  period: string;
  remarks?: string;
  payments: Array<{
    type: string;
    ids: number[];
  }>;
}

// ✅ NEW: Error response interface
export interface PayrollError {
  type: 'DUPLICATE_PERIOD' | 'GENERAL_ERROR' | 'VALIDATION_ERROR';
  message: string;
  period?: string;
  statusCode?: number;
}

@Injectable({
  providedIn: 'root',
})
export class PayrollService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getOrganizationEmployees(orgId: number): Observable<Employee[]> {
    console.log(`📡 Fetching employees for org: ${orgId}`);
    return this.http.get<Employee[]>(`${this.baseUrl}/employees/org/${orgId}`).pipe(
      catchError(this.handleError)
    );
  }

  getOrganizationAdmins(orgId: number): Observable<OrgAdmin[]> {
    console.log(`📡 Fetching admins for org: ${orgId}`);
    return this.http.get<OrgAdmin[]>(`${this.baseUrl}/orgadmins/org/${orgId}`).pipe(
      catchError(this.handleError)
    );
  }

  // payroll-service.ts

createSalaryDisbursalRequest(request: SalaryDisbursalRequest): Observable<any> {
  console.log('📤 Creating salary disbursal request:', request);
  
  return this.http.post(`${this.baseUrl}/salary-disbursal/request`, request).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('❌ Salary disbursal request failed:', error);
      console.error('   Status:', error.status);
      console.error('   Error Body:', error.error);
      
      // ✅ Extract error message with multiple fallbacks
      let errorMessage = 'Failed to create salary disbursal request';
      
      if (error.error) {
        // Try different paths for error message
        if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.error) {
          errorMessage = error.error.error;
        } else if (typeof error.error === 'string') {
          errorMessage = error.error;
        }
      }
      
      // If no message found, use status text
      if (errorMessage === 'Failed to create salary disbursal request' && error.statusText) {
        errorMessage = `${error.statusText}: ${error.message || 'Unknown error'}`;
      }
      
      console.log('📝 Parsed error message:', errorMessage);
      
      // ✅ Detect duplicate error
      const isDuplicateError = 
        errorMessage.toLowerCase().includes('already exists') || 
        errorMessage.toLowerCase().includes('duplicate') ||
        errorMessage.toLowerCase().includes('pending') ||
        error.error?.error === 'Duplicate Payroll Request' ||
        error.error?.period;
      
      if (isDuplicateError) {
        console.warn('⚠️ Duplicate period detected');
        
        const payrollError: PayrollError = {
          type: 'DUPLICATE_PERIOD',
          message: errorMessage,
          period: error.error?.period || request.period,
          statusCode: error.status
        };
        
        return throwError(() => payrollError);
      }
      
      // ✅ General error
      const payrollError: PayrollError = {
        type: 'GENERAL_ERROR',
        message: errorMessage,
        statusCode: error.status
      };
      
      return throwError(() => payrollError);
    })
  );
}


  getPendingDisbursals(orgId: number): Observable<any[]> {
    console.log(`📡 Fetching pending disbursals for org: ${orgId}`);
    return this.http.get<any[]>(`${this.baseUrl}/salary-disbursal/org/${orgId}/pending`).pipe(
      catchError(this.handleError)
    );
  }

  // ✅ Generic error handler
  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('❌ HTTP Error:', error);
    
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = error.error?.message || error.message || `Error Code: ${error.status}`;
    }
    
    const payrollError: PayrollError = {
      type: 'GENERAL_ERROR',
      message: errorMessage,
      statusCode: error.status
    };
    
    return throwError(() => payrollError);
  }
}
