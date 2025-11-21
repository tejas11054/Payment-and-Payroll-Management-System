import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// ═══════════════════════════════════════════════════════════════════
// INTERFACES
// ═══════════════════════════════════════════════════════════════════

export interface VendorRequest {
  name: string;
  vendorType: string;
  bankName: string;
  bankAccountNo: string;
  ifscCode: string;
  contactEmail: string;
  phone: string;
  fileUrl?: string;
  balance?: number;
}

export interface VendorResponse {
  vendorId: number;
  name: string;
  vendorType: string;
  bankName: string;
  bankAccountNo: string;
  ifscCode: string;
  contactEmail: string;
  phone: string;
  deleted: boolean;
  createdAt: string;
  organizationId: number;
  balance: number;
}

export interface PaymentRequest {
  paymentId?: number;
  amount: number;
  invoiceReference: string;
  status?: string;
  orgId: number;
  vendorId: number;
  requestedById: number;
  approvedById?: number;
  createdAt?: string;
  processedAt?: string;
}

// ✅ NEW: Vendor Profile Interface
export interface VendorProfile {
  vendorId: number;
  name: string;
  contactEmail: string;
  phone: string;
  vendorType: string;
  balance: number;
  bankName: string;
  bankAccountNo: string;
  ifscCode: string;
  accountHolderName: string;
  orgId: number;
  orgName: string;
  createdAt: string;
}

// ✅ NEW: Payment Receipt Interface
export interface PaymentReceipt {
  receiptId: number;
  paymentId: number;
  amount: number;
  bankReference: string;
  status: string;
  vendorId: number;
  vendorName: string;
  orgId: number;
  orgName: string;
  createdAt: string;
}

// ✅ NEW: Change Password DTO
export interface ChangePasswordDTO {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class VendorService {
  
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/vendors';
  private paymentUrl = 'http://localhost:8080/api/payment-requests';

  // ═══════════════════════════════════════════════════════════════════
  // EXISTING METHODS
  // ═══════════════════════════════════════════════════════════════════

  /**
   * Create single vendor
   */
  createVendor(orgId: number, vendorData: VendorRequest, documentFile?: File): Observable<VendorResponse> {
    const formData = new FormData();
    const dtoBlob = new Blob([JSON.stringify(vendorData)], { type: 'application/json' });
    formData.append('dto', dtoBlob);
    
    if (documentFile) {
      formData.append('documentFile', documentFile);
    }
    
    return this.http.post<VendorResponse>(`${this.baseUrl}/${orgId}`, formData);
  }

  /**
   * Bulk upload vendors
   */
  uploadVendorsBulk(
    orgId: number, 
    file: File, 
    fileUrl?: string, 
    documentFile?: File
  ): Observable<VendorResponse[]> {
    const formData = new FormData();
    formData.append('file', file);
    
    if (fileUrl) {
      formData.append('fileUrl', fileUrl);
    }
    
    if (documentFile) {
      formData.append('documentFile', documentFile);
    }
    
    return this.http.post<VendorResponse[]>(
      `${this.baseUrl}/${orgId}/upload-vendors`, 
      formData
    );
  }

  /**
   * Get all vendors for organization
   */
  getVendorsByOrganization(orgId: number): Observable<VendorResponse[]> {
    return this.http.get<VendorResponse[]>(`${this.baseUrl}/${orgId}`);
  }

  /**
   * Get vendor by ID
   */
  getVendorById(vendorId: number): Observable<VendorResponse> {
    return this.http.get<VendorResponse>(`${this.baseUrl}/vendor/${vendorId}`);
  }

  /**
   * Update vendor
   */
  updateVendor(vendorId: number, vendorData: Partial<VendorRequest>): Observable<VendorResponse> {
    return this.http.put<VendorResponse>(`${this.baseUrl}/vendor/${vendorId}`, vendorData);
  }

  /**
   * Delete vendor
   */
  deleteVendor(vendorId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/vendor/${vendorId}`);
  }

  /**
   * Create payment request
   */
  createPaymentRequest(paymentData: PaymentRequest): Observable<PaymentRequest> {
    return this.http.post<PaymentRequest>(this.paymentUrl, paymentData);
  }

  /**
   * Get vendor statistics
   */
  getVendorStats(orgId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${orgId}/stats`);
  }

  // ═══════════════════════════════════════════════════════════════════
  // ✅ NEW: VENDOR DASHBOARD METHODS
  // ═══════════════════════════════════════════════════════════════════

  /**
   * GET /api/vendors/my-profile
   * Get logged in vendor's profile
   */
  getMyProfile(): Observable<VendorProfile> {
    return this.http.get<VendorProfile>(`${this.baseUrl}/my-profile`);
  }

  /**
   * PUT /api/vendors/my-profile
   * Update logged in vendor's profile
   */
  updateMyProfile(profileData: Partial<VendorProfile>): Observable<VendorProfile> {
    return this.http.put<VendorProfile>(`${this.baseUrl}/my-profile`, profileData);
  }

  /**
   * GET /api/vendors/my-receipts
   * Get all receipts for logged in vendor
   */
  getMyReceipts(): Observable<PaymentReceipt[]> {
    return this.http.get<PaymentReceipt[]>(`${this.baseUrl}/my-receipts`);
  }

  /**
   * GET /api/vendors/receipt/{receiptId}
   * Get single receipt details
   */
  getReceiptDetails(receiptId: number): Observable<PaymentReceipt> {
    return this.http.get<PaymentReceipt>(`${this.baseUrl}/receipt/${receiptId}`);
  }

  /**
   * GET /api/vendors/receipt/{receiptId}/download
   * Download receipt as text file
   */
  downloadReceipt(receiptId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/receipt/${receiptId}/download`, {
      responseType: 'blob'
    });
  }

  /**
   * POST /api/vendors/change-password
   * Change vendor password
   */
  changePassword(passwordData: ChangePasswordDTO): Observable<string> {
    return this.http.post(`${this.baseUrl}/change-password`, passwordData, {
      responseType: 'text'
    });
  }
}
