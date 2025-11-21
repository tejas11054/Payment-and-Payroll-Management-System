
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginAuthentication } from '../login/login-authentication';
import { DepartmentRequestDTO, DepartmentResponseDTO } from '../../DTO\'s/DepartmentDTO';


@Injectable({
  providedIn: 'root'
})
export class Department {
    private http = inject(HttpClient);
  private authService = inject(LoginAuthentication);
  private baseUrl = 'http://localhost:8080/api/departments';

  getAllByOrg(): Observable<DepartmentResponseDTO[]> {
    const orgId = this.authService.getOrgIdFromToken();
    return this.http.get<DepartmentResponseDTO[]>(`${this.baseUrl}/org/${orgId}`);
  }

  createDepartment(dto: DepartmentRequestDTO): Observable<DepartmentResponseDTO> {
    const orgId = this.authService.getOrgIdFromToken();
    return this.http.post<DepartmentResponseDTO>(`${this.baseUrl}/${orgId}`, dto);
  }

  updateDepartment(id: number, dto: DepartmentRequestDTO): Observable<DepartmentResponseDTO> {
    return this.http.put<DepartmentResponseDTO>(`${this.baseUrl}/${id}`, dto);
  }

  deleteDepartment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
  
}
