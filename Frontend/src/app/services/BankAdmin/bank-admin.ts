import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { OrganizationResponseDTO } from '../../OrganizationResponesDTO/OrganizationResponseDTO';
import { ContactMessage } from "../../EmployeeDTO's/EmployeeDTO";


export interface VendorPaymentRequest {
  paymentId: number;
  orgId: number;
  orgName?: string;
  vendorId: number;
  vendorName?: string;
  amount: number;
  bankAccountNo: string;
  ifscCode: string;
  bankName: string;
  invoiceReference: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';
  requestedById: number;
  requestedByName?: string;
  createdAt: string;
  approvedById?: number;
  processedAt?: string;
  vendorType?: string; // e.g., "LOCAL", "INTERNATIONAL"
}

@Injectable({
  providedIn: 'root',
})
export class BankAdmin {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/bank-admin';

  getPendingOrganizations(): Observable<OrganizationResponseDTO[]> {
    return this.http.get<OrganizationResponseDTO[]>(`${this.baseUrl}/pending-organizations`);
  }

  getAllApprovedOrganizations(): Observable<OrganizationResponseDTO[]> {
    return this.http.get<OrganizationResponseDTO[]>(`${this.baseUrl}/organizations`);
  }

  approveOrganization(orgId: number): Observable<string> {
    return this.http.put(`${this.baseUrl}/${orgId}/approve`, {}, { responseType: 'text' });
  }

  rejectOrganization(orgId: number, reason: string): Observable<string> {
    return this.http.put(`${this.baseUrl}/${orgId}/reject`, { reason }, { responseType: 'text' });
  }

  handleDeletionRequest(orgId: number, approve: boolean, reason: string): Observable<string> {
    const params = { approve: approve.toString(), reason: reason };
    return this.http.put(`${this.baseUrl}/${orgId}/handle-deletion`, null, {
      params,
      responseType: 'text',
    });
  }

  getContactMessages(): Observable<ContactMessage[]> {
    return this.http.get<ContactMessage[]>(`${this.baseUrl}/contact-messages`);
  }

  markMessageAsRead(messageId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/contact-messages/${messageId}/read`, {});
  }

  getDeletionRequestedOrgs(): Observable<OrganizationResponseDTO[]> {
    return this.http.get<OrganizationResponseDTO[]>(`${this.baseUrl}/delete-requests`);
  }

  getOrganizationDetails(orgId: number): Observable<any> {
    return this.http.get(`http://localhost:8080/api/organizations/${orgId}/details`);
  }

  getPendingSalaryRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/salary-requests/pending`);
  }

  getSalaryRequestDetails(disbursalId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/salary-requests/${disbursalId}`);
  }

  approveSalaryRequest(disbursalId: number, action: string, comment: string): Observable<any> {
    return this.http.post(
      `http://localhost:8080/api/salary-disbursal/approve-or-reject`,
      {
        disbursalRequestId: disbursalId,
        action: action,
        comment: comment,
      },
      {
        responseType: 'text' as 'json',
      }
    );
  }

  // ═════════════════════════════════════════════════════════════════
  // ✅ VENDOR PAYMENT REQUEST METHODS
  // ═════════════════════════════════════════════════════════════════

  /**
   * Get all pending vendor payment requests
   */
  getPendingPaymentRequests(): Observable<VendorPaymentRequest[]> {
    return this.http.get<VendorPaymentRequest[]>(
      `http://localhost:8080/api/payment-requests/pending`
    );
  }

  /**
   * Get payment request details by ID
   */
  getPaymentRequestDetails(paymentId: number): Observable<VendorPaymentRequest> {
    return this.http.get<VendorPaymentRequest>(
      `http://localhost:8080/api/payment-requests/${paymentId}`
    );
  }

  /**
   * Approve vendor payment request
   */
  approvePaymentRequest(paymentId: number, comment: string): Observable<VendorPaymentRequest> {
    return this.http.put<VendorPaymentRequest>(
      `http://localhost:8080/api/payment-requests/${paymentId}/approve`,
      { comment }
    );
  }

  /**
   * Reject vendor payment request
   */
  rejectPaymentRequest(paymentId: number, reason: string): Observable<VendorPaymentRequest> {
    return this.http.put<VendorPaymentRequest>(
      `http://localhost:8080/api/payment-requests/${paymentId}/reject`,
      { reason }
    );
  }

  /**
   * Get all payment requests (for history/logs)
   */
  getAllPaymentRequests(): Observable<VendorPaymentRequest[]> {
    return this.http.get<VendorPaymentRequest[]>(
      `http://localhost:8080/api/payment-requests`
    );
  }

  /**
   * Get payment requests by organization
   */
  getPaymentRequestsByOrg(orgId: number): Observable<VendorPaymentRequest[]> {
    return this.http.get<VendorPaymentRequest[]>(
      `http://localhost:8080/api/payment-requests/org/${orgId}`
    );
  }
}
