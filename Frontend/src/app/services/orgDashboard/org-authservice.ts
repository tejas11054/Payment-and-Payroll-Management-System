import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, of, throwError } from 'rxjs';
import { LoginAuthentication } from '../login/login-authentication';
import {
  EmployeeDTO,
  EmployeeRequestDTO,
  EmployeeResponseDTO,
} from "../../EmployeeDTO's/EmployeeDTO";

interface DecodedToken {
  sub: string;
  roles: string[];
  orgId?: number;
  orgStatus?: 'PENDING' | 'APPROVED' | 'REJECTED';
  exp: number;
}

@Injectable({
  providedIn: 'root',
})
export class OrgAuthservice {
  private http = inject(HttpClient);
  private authService = inject(LoginAuthentication);

  // ✅ FIXED: Changed base URL to /api/employees
  private baseUrl = 'http://localhost:8080/api/employees';

  /**
   * Get all employees in an organization
   */
  getAllEmployees(orgId: number): Observable<EmployeeResponseDTO[]> {
    // ✅ CORRECT: GET /api/employees/org/{orgId}
    return this.http.get<EmployeeResponseDTO[]>(`${this.baseUrl}/org/${orgId}`);
  }

  /**
   * Add a single employee with optional document
   */
  addEmployee(employeeData: EmployeeRequestDTO, documentFile?: File): Observable<EmployeeDTO> {
    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) {
      console.error('Organization ID not found, cannot add employee.');
      return throwError(() => new Error('Organization ID not found'));
    }

    const formData = new FormData();

    // Key 'employee' must match @RequestPart("employee") in backend
    formData.append(
      'employee',
      new Blob([JSON.stringify(employeeData)], { type: 'application/json' })
    );

    // Key 'document' must match @RequestPart("document") in backend
    if (documentFile) {
      formData.append('document', documentFile, documentFile.name);
    }

    // ✅ CORRECT: POST /api/employees/{orgId}/employees
    return this.http.post<EmployeeDTO>(
      `${this.baseUrl}/${orgId}/employees`,
      formData
    );
  }

  /**
   * Bulk upload employees from file
   */
  addEmployeesBatch(
    file: File, 
    departmentName: string, 
    salaryGradeId?: number
  ): Observable<EmployeeDTO[]> {
    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) {
      console.error('Organization ID not found, cannot add batch.');
      return throwError(() => new Error('Organization ID not found'));
    }
    
    const formData = new FormData();
    formData.append('departmentName', departmentName);
    formData.append('file', file, file.name);
    
    if (salaryGradeId && salaryGradeId > 0) {
      formData.append('salaryGradeId', salaryGradeId.toString());
    }
    
    // ✅ CORRECT: POST /api/employees/{orgId}/employees/bulk-upload
    return this.http.post<EmployeeDTO[]>(
      `${this.baseUrl}/${orgId}/employees/bulk-upload`,
      formData
    );
  }

  /**
   * Update an existing employee
   */
  updateEmployee(orgId: number, empId: number, dto: EmployeeRequestDTO): Observable<EmployeeResponseDTO> {
    // ✅ FIXED: PUT /api/employees/{orgId}/employees/{empId}
    return this.http.put<EmployeeResponseDTO>(
      `${this.baseUrl}/${orgId}/employees/${empId}`,
      dto
    );
  }

  /**
   * Delete (soft delete) an employee
   */
  deleteEmployee(orgId: number, empId: number): Observable<void> {
    // ✅ FIXED: DELETE /api/employees/{orgId}/employees/{empId}
    return this.http.delete<void>(
      `${this.baseUrl}/${orgId}/employees/${empId}`
    );
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error.error?.message || error.message || `Server returned code ${error.status}`;
    }
    
    console.error('HTTP Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  // ========================================================================
  // Department-related methods
  // ========================================================================

  getAllDepartments(orgId: number): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8080/api/departments/org/${orgId}`);
  }

  addDepartment(orgId: number, dept: any): Observable<any> {
    return this.http.post<any>(`http://localhost:8080/api/departments/${orgId}`, dept);
  }

  deleteDepartment(id: number): Observable<void> {
    return this.http.delete<void>(`http://localhost:8080/api/departments/${id}`);
  }

  updateDepartment(id: number, body: any): Observable<any> {
    return this.http.put<any>(`http://localhost:8080/api/departments/${id}`, body);
  }
getEmployeeCountForDepartment(departmentId: number): Observable<number> {
  return this.http.get<number>(
    `http://localhost:8080/api/departments/${departmentId}/employee-count`
  );
}

/**
 * Get count of active admins in a department
 */
getAdminCountForDepartment(departmentId: number): Observable<number> {
  return this.http.get<number>(
    `http://localhost:8080/api/departments/${departmentId}/admin-count`
  );
}
  // ========================================================================
  // Account balance methods
  // ========================================================================

  getAccountBalance(orgId: number): Observable<number> {
    return this.http.get<number>(
      `http://localhost:8080/api/organizations/${orgId}/account-balance`
    );
  }

  getMyAccountBalance(): Observable<number> {
    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) {
      return throwError(() => new Error('Organization ID not found'));
    }
    return this.getAccountBalance(orgId);
  }
}
