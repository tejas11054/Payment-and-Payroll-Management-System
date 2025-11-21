import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { LoginAuthentication } from '../login/login-authentication';
import { Observable } from 'rxjs';
export interface SalaryGradeRequestDTO {
  gradeCode: string;
  basicSalary: number;
  hra: number;
  da: number;
  pf: number;
  allowances: number;
}

export interface SalaryGradeResponseDTO {
  gradeId: number;
  gradeCode: string;
  basicSalary: number;
  hra: number;
  da: number;
  pf: number;
  allowances: number;
  organizationId: number;
}
@Injectable({
  providedIn: 'root'
})
export class SalaryGrade {
   private http = inject(HttpClient);
  private authService = inject(LoginAuthentication);

  private baseUrl = 'http://localhost:8080/api/salary-grades';

  // Get all salary grades for organization
  getAllSalaryGrades(orgId: number): Observable<SalaryGradeResponseDTO[]> {
    return this.http.get<SalaryGradeResponseDTO[]>(`${this.baseUrl}/${orgId}`);
  }

  // Get single salary grade
  getSalaryGrade(orgId: number, gradeId: number): Observable<SalaryGradeResponseDTO> {
    return this.http.get<SalaryGradeResponseDTO>(`${this.baseUrl}/${orgId}/${gradeId}`);
  }

  // Create new salary grade
  createSalaryGrade(orgId: number, grade: SalaryGradeRequestDTO): Observable<SalaryGradeResponseDTO> {
    return this.http.post<SalaryGradeResponseDTO>(`${this.baseUrl}/${orgId}`, grade);
  }

  // Update salary grade
  updateSalaryGrade(orgId: number, gradeId: number, grade: SalaryGradeRequestDTO): Observable<SalaryGradeResponseDTO> {
    return this.http.put<SalaryGradeResponseDTO>(`${this.baseUrl}/${orgId}/${gradeId}`, grade);
  }

  // Delete salary grade
  deleteSalaryGrade(orgId: number, gradeId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${orgId}/${gradeId}`);
  }

  // Helper method to get orgId from token
  getMyOrgId(): number | null {
    return this.authService.getOrgIdFromToken();
  }
}
