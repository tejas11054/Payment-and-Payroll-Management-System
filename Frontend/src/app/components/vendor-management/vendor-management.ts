// vendor-management.component.ts

import { ChangeDetectorRef, Component, inject, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/notification/toast-service';
import { AddVendorModal } from '../add-vendor-modal/add-vendor-modal';
import { VendorResponse, VendorService, PaymentRequest } from '../../services/vendor/vendor'; // ✅ ADD PaymentRequest
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ConfirmationConfigModal } from '../../services/ConfirmationConfig/confirmation-config-modal';
import { LoginAuthentication } from '../../services/login/login-authentication';

@Component({
  selector: 'app-vendor-management',
  standalone: true,
  imports: [CommonModule, AddVendorModal, FormsModule, ReactiveFormsModule],
  templateUrl: './vendor-management.html',
  styleUrl: './vendor-management.css',
  encapsulation: ViewEncapsulation.None
})
export class VendorManagementComponent implements OnInit {
  
  @Input() orgId!: number;
  
  private vendorService = inject(VendorService);
  private toastService = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);
  private confirmationService = inject(ConfirmationConfigModal);
  private authService = inject(LoginAuthentication);

  vendors: VendorResponse[] = [];
  filteredVendors: VendorResponse[] = [];
  isLoading = false;
  isAddVendorModalOpen = false;
  searchQuery = '';
  selectedVendorType = 'all';

  vendorTypes = ['all', 'Service Provider', 'Product Supplier', 'Consultant', 'Contractor', 'Freelancer', 'Other'];
  
  constructor() {
    this.isAddVendorModalOpen = false;
    console.log('VendorManagement Constructor - Modal State:', this.isAddVendorModalOpen);
  }
  
  ngOnInit(): void {
    this.isAddVendorModalOpen = false;
    this.cdr.detectChanges();
    
    if (!this.orgId) {
      console.error('Organization ID not found');
      this.toastService.show('Organization ID not found', 'error');
      return;
    }
    
    this.loadVendors();
  }

  loadVendors(): void {
    console.log('Loading vendors for orgId:', this.orgId);
    this.isLoading = true;
    
    this.vendorService.getVendorsByOrganization(this.orgId).subscribe({
      next: (data) => {
        console.log('Vendors loaded successfully:', data.length);
        this.vendors = data;
        this.filteredVendors = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading vendors:', err);
        this.toastService.show('Failed to load vendors', 'error');
        this.isLoading = false;
        this.vendors = [];
        this.filteredVendors = [];
      }
    });
  }

  openAddVendorModal(): void {
    console.log('Opening Add Vendor Modal');
    this.isAddVendorModalOpen = true;
    this.cdr.detectChanges();
  }

  closeAddVendorModal(): void {
    console.log('Closing Add Vendor Modal');
    this.isAddVendorModalOpen = false;
    this.cdr.detectChanges();
  }

  onVendorCreated(): void {
    console.log('Vendor created - reloading list');
    this.loadVendors();
  }

  onSearch(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredVendors = this.vendors.filter(vendor => {
      const matchesSearch = vendor.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
                          vendor.contactEmail.toLowerCase().includes(this.searchQuery.toLowerCase());
      
      const matchesType = this.selectedVendorType === 'all' || vendor.vendorType === this.selectedVendorType;
      
      return matchesSearch && matchesType && !vendor.deleted;
    });
  }

  // ✅ DELETE VENDOR
  deleteVendor(vendorId: number, vendorName: string): void {
    this.confirmationService.confirm({
      title: 'Delete Vendor',
      message: `Are you sure you want to delete vendor "${vendorName}"? This action cannot be undone.`,
      confirmText: 'Yes, Delete',
      cancelText: 'Cancel',
      confirmCallback: () => {
        this.vendorService.deleteVendor(vendorId).subscribe({
          next: () => {
            this.toastService.show(`Vendor "${vendorName}" deleted successfully`, 'success');
            this.loadVendors();
          },
          error: (err) => {
            console.error('Error deleting vendor:', err);
            this.toastService.show('Failed to delete vendor', 'error');
          }
        });
      }
    });
  }

  // ✅ MAKE PAYMENT
  makePayment(vendor: VendorResponse): void {
    this.confirmationService.confirm({
      title: 'Request Payment Approval',
      message: `Create payment request for vendor "${vendor.name}"?\n\nAmount: ₹${vendor.balance.toLocaleString('en-IN')}\nBank: ${vendor.bankName}\nAccount: ${vendor.bankAccountNo}\n\nThis request will be sent to Bank Admin for approval.`,
      confirmText: 'Create Request',
      cancelText: 'Cancel',
      confirmCallback: () => {
        this.createPaymentRequest(vendor);
      }
    });
  }

  // ✅ CREATE PAYMENT REQUEST
  private createPaymentRequest(vendor: VendorResponse): void {
    const currentUserId = this.authService.getUserIdFromToken() || 0;
    const invoiceRef = `INV-${vendor.vendorId}-${Date.now()}`;
    
    const paymentData: PaymentRequest = {
      amount: vendor.balance,
      invoiceReference: invoiceRef,
      orgId: this.orgId,
      vendorId: vendor.vendorId,
      requestedById: currentUserId,
      status: 'PENDING'
    };

    console.log('Creating payment request:', paymentData);

    this.vendorService.createPaymentRequest(paymentData).subscribe({
      next: (response) => {
        this.toastService.show(
          `✅ Payment Request Created!\n\nRequest ID: #${response.paymentId}\nInvoice: ${invoiceRef}\nVendor: ${vendor.name}\n\nStatus: Awaiting Bank Admin Approval`,
          'success'
        );
        console.log('Payment request created:', response);
      },
      error: (err) => {
        console.error('Error creating payment request:', err);
        this.toastService.show(
          err?.error?.message || 'Failed to create payment request. Please try again.',
          'error'
        );
      }
    });
  }

  // ✅ EDIT VENDOR
  editVendor(vendor: VendorResponse): void {
    this.toastService.show(`Edit vendor "${vendor.name}" - Coming soon!`, 'warning');
    console.log('Edit vendor:', vendor);
  }

  getTotalVendors(): number {
    return this.vendors.filter(v => !v.deleted).length;
  }

  getActiveVendors(): number {
    return this.vendors.filter(v => !v.deleted).length;
  }
}
