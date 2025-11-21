import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginAuthentication } from '../login/login-authentication';
import { OrgAdminRequestDTO, OrgAdminResponseDTO } from '../../DTO\'s/DepartmentDTO';


@Injectable({
  providedIn: 'root'
})
export class OrgAdminService {
  private http = inject(HttpClient);
  private authService = inject(LoginAuthentication);
  private baseUrl = 'http://localhost:8080/api/orgadmins';

  /**
   * ✅ Get all Org Admins for the currently logged-in organization.
   * Org ID is fetched from the JWT token, so we don’t need to pass it manually.
   */
  getAdminsForCurrentOrg(): Observable<OrgAdminResponseDTO[]> {
    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) throw new Error('Organization ID not found in token');

    return this.http.get<OrgAdminResponseDTO[]>(`${this.baseUrl}/${orgId}`);
  }

  /**
   * ✅ Create a single Org Admin.
   * Uses multipart/form-data since we are uploading a document.
   */
  createOrgAdmin(dto: OrgAdminRequestDTO, documentFile?: File): Observable<OrgAdminResponseDTO> {
    
    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) throw new Error('Organization ID not found in token');

    const formData = new FormData();
    // Convert DTO to JSON string
    formData.append('dto', new Blob([JSON.stringify(dto)], { type: 'application/json' }));

    // Attach document file if provided
    if (documentFile) {
      formData.append('documentFile', documentFile, documentFile.name);
    }

    return this.http.post<OrgAdminResponseDTO>(`${this.baseUrl}/${orgId}`, formData);
  }

  /**
   * ✅ Update an Org Admin.
   * userId no longer required — backend gets it from JWT automatically.
   */
  updateOrgAdmin(adminId: number, dto: OrgAdminRequestDTO): Observable<OrgAdminResponseDTO> {
    return this.http.put<OrgAdminResponseDTO>(`${this.baseUrl}/admin/${adminId}`, dto);
  }

  /**
   * ✅ Delete an Org Admin.
   * userId no longer required — backend gets it from JWT automatically.
   */
  deleteOrgAdmin(adminId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/admin/${adminId}`);
  }
}
