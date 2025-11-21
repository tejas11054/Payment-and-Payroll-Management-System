import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface AuditLog {
  logId: number;
  performedByEmail: string;
  performedByUserId: number;
  performedByRole: string;
  actionPerformed: string;
  targetResourceType: string;
  targetResourceId: number;
  actionTimestamp: string;
}

export interface AuditLogFilter {
  role?: string;
  action?: string;
  resourceType?: string;
  startDate?: string;
  endDate?: string;
  userId?: number;
}
@Injectable({
  providedIn: 'root'
})
export class BankadminService {
 private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/audit-logs';

  /**
   * Get all audit logs
   */
  getAllAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.baseUrl);
  }

  /**
   * Get audit logs with filters
   */
  getFilteredAuditLogs(filters: AuditLogFilter): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/filter`, { params: filters as any });
  }

  /**
   * Get audit logs by user ID
   */
  getAuditLogsByUser(userId: number): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/user/${userId}`);
  }

  /**
   * Get audit logs by role
   */
  getAuditLogsByRole(role: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/role/${role}`);
  }

  /**
   * Get audit logs by action type
   */
  getAuditLogsByAction(action: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/action/${action}`);
  }

  /**
   * Get audit logs by resource type
   */
  getAuditLogsByResourceType(resourceType: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/resource/${resourceType}`);
  }

  /**
   * Get audit logs count
   */
  getAuditLogsCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/count`);
  }
}