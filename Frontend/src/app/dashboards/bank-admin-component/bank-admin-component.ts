import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BankAdmin, VendorPaymentRequest } from '../../services/BankAdmin/bank-admin';
import { DarkModeService } from '../../services/darkMode/dark-mode';
import { ToastService } from '../../services/notification/toast-service';
import { ConfirmationConfigModal } from '../../services/ConfirmationConfig/confirmation-config-modal';
import { LoginAuthentication } from '../../services/login/login-authentication';
import { BankAdminAuditLog } from '../../components/Audit-logs/bank-admin-audit-log/bank-admin-audit-log';


@Component({
  selector: 'app-bank-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule,BankAdminAuditLog],
  templateUrl: './bank-admin-component.html',
  styleUrls: ['./bank-admin-component.css'],
})
export class BankAdminComponent implements OnInit {
  // ================= View & State =================
  currentView: string = 'pending';
  isLoading: boolean = true;

  // ================= Dark Mode =================
  isDarkMode: boolean = false;

  // ================= Data Streams =================
  pendingOrgs$!: Observable<any[]>;
  approvedOrgs$!: Observable<any[]>;
  contactMessages$!: Observable<any[]>;
  pendingSalaryRequests$!: Observable<any[]>;
  pendingPaymentRequests$!: Observable<VendorPaymentRequest[]>;
  pendingPaymentCount: number = 0;

  // Payment Request Modals
  isPaymentDetailsModalOpen: boolean = false;
  selectedPaymentRequest: any = null;
  isPaymentApprovalModalOpen: boolean = false;
  isPaymentRejectionModalOpen: boolean = false;
  paymentApprovalComment: string = '';
  paymentRejectionReason: string = '';
  currentPaymentIdForAction: number | null = null;

  // ================= Counts =================
  unreadCount: number = 0;
  pendingSalaryCount: number = 0;

  // ================= Organization Modals =================
  isRejectModalOpen: boolean = false;
  isDetailsModalOpen: boolean = false;
  selectedOrg: any = null;
  rejectionReason: string = '';

  // ================= Salary Modals =================
  isSalaryDetailsModalOpen = false;
  selectedSalaryRequest: any = null;

  isApprovalModalOpen: boolean = false;
  approvalComment: string = '';
  currentDisbursalIdForApproval: number | null = null;

  isRejectSalaryModalOpen: boolean = false;
  salaryRejectionReason: string = '';
  currentDisbursalIdForRejection: number | null = null;

  // ================= Services =================
  darkModeService = inject(DarkModeService);
  bankAdminService = inject(BankAdmin);
  router = inject(Router);
  toastService = inject(ToastService);
  confirmService = inject(ConfirmationConfigModal);
  cdr = inject(ChangeDetectorRef);
  ngZone = inject(NgZone);
  authservices = inject(LoginAuthentication);

  // ================= Lifecycle =================
  ngOnInit(): void {
    this.darkModeService.isDarkMode$.subscribe((isDark: boolean) => {
      this.isDarkMode = isDark;
      this.cdr.markForCheck();
    });

    this.loadDashboardData();
  }

  // ================= Theme =================
  toggleTheme(): void {
    this.darkModeService.toggle();
  }

  // ================= Load Dashboard Data =================
  loadDashboardData(): void {
    this.isLoading = true;

    this.pendingOrgs$ = this.bankAdminService.getPendingOrganizations();
    this.approvedOrgs$ = this.bankAdminService.getAllApprovedOrganizations();
    this.contactMessages$ = this.bankAdminService.getContactMessages();
    this.pendingSalaryRequests$ = this.bankAdminService.getPendingSalaryRequests();
    this.pendingPaymentRequests$ = this.bankAdminService.getPendingPaymentRequests();

    this.contactMessages$
      .pipe(map((messages) => messages.filter((msg) => msg.status === 'UNREAD').length))
      .subscribe((count) => {
        this.unreadCount = count;
        this.cdr.markForCheck();
      });

    this.pendingSalaryRequests$.pipe(map((requests) => requests.length)).subscribe((count) => {
      this.pendingSalaryCount = count;
      this.cdr.markForCheck();
    });
    this.pendingPaymentRequests$.pipe(map((requests) => requests.length)).subscribe((count) => {
      this.pendingPaymentCount = count;
      this.cdr.markForCheck();
    });
    this.ngZone.run(() => {
      setTimeout(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }, 800);
    });
  }

  // ================= View Navigation =================
  setView(view: string): void {
    this.currentView = view;
  }

  // ================= Approve Organization =================
  approveOrganization(orgId: number): void {
    this.confirmService.confirm({
      title: 'Approve Organization',
      message: 'Are you sure you want to approve this organization?',
      confirmText: 'Approve',
      cancelText: 'Cancel',
      confirmCallback: () => {
        this.bankAdminService.approveOrganization(orgId).subscribe({
          next: () => {
            this.toastService.show('Organization approved successfully!', 'success');
            this.loadDashboardData();
            this.cdr.markForCheck();
          },
          error: (error) => {
            console.error('Error approving organization:', error);
            this.toastService.show('Failed to approve organization. Please try again.', 'error');
          },
        });
      },
    });
  }

  // ================= Organization Rejection =================
  openRejectModal(org: any): void {
    this.selectedOrg = org;
    this.rejectionReason = '';
    this.isRejectModalOpen = true;
    this.cdr.markForCheck();
  }

  closeRejectModal(): void {
    this.isRejectModalOpen = false;
    this.selectedOrg = null;
    this.rejectionReason = '';
    this.cdr.markForCheck();
  }

  confirmRejection(): void {
    if (!this.rejectionReason.trim()) {
      this.toastService.show('Please provide a reason for rejection.', 'error');
      return;
    }

    if (!this.selectedOrg) return;

    this.bankAdminService
      .rejectOrganization(this.selectedOrg.orgId, this.rejectionReason)
      .subscribe({
        next: () => {
          this.toastService.show('Organization rejected successfully!', 'success');
          this.closeRejectModal();
          this.loadDashboardData();
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error('Error rejecting organization:', error);
          this.toastService.show('Failed to reject organization. Please try again.', 'error');
        },
      });
  }

  // ================= Organization Details Modal =================
  openDetailsModal(org: any): void {
    // Show modal instantly with basic info
    this.selectedOrg = org;
    this.isDetailsModalOpen = true;
    this.cdr.markForCheck();

    // Fetch full details in the background
    this.bankAdminService.getOrganizationDetails(org.orgId).subscribe({
      next: (details) => {
        this.selectedOrg = details; // Update with full details
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('Error fetching organization details:', error);
        this.toastService.show('Failed to load full organization details.', 'error');
      },
    });
  }

  closeDetailsModal(): void {
    this.isDetailsModalOpen = false;
    this.selectedOrg = null;
    this.cdr.markForCheck();
  }

  // ================= Salary Request Details Modal =================
  openSalaryDetailsModal(request: any): void {
    console.log('Opening salary details modal for request:', request);
  this.bankAdminService.getSalaryRequestDetails(request.disbursalId).subscribe({
    next: (details) => {
      this.selectedSalaryRequest = details;
      this.isSalaryDetailsModalOpen = true; // ✅ Modal open
      this.cdr.markForCheck();
      
      // Background में data load करो
      this.bankAdminService.getOrganizationDetails(details.orgId).subscribe({
        next: (orgDetails) => {
          this.selectedSalaryRequest.orgBalance = orgDetails.accountBalance;
          this.cdr.markForCheck();
        },
        error: () => {
          this.cdr.markForCheck();
        }
      });
    },
    error: (error) => {
      console.error('Error:', error);
      this.toastService.show('Failed to load details.', 'error');
    }
  });
}

  closeSalaryDetailsModal(): void {
    this.isSalaryDetailsModalOpen = false;
    this.selectedSalaryRequest = null;
    this.cdr.markForCheck();
  }

  // ================= Salary Approval Modal =================
  approveSalaryRequest(disbursalId: number | undefined): void {
    if (!disbursalId) {
      this.toastService.show('Invalid salary request.', 'error');
      return;
    }

    this.currentDisbursalIdForApproval = disbursalId;
    this.approvalComment = '';

    this.isSalaryDetailsModalOpen = false;
    this.isApprovalModalOpen = true;

    this.cdr.markForCheck();
  }

  closeApprovalModal(): void {
    this.isApprovalModalOpen = false;
    this.currentDisbursalIdForApproval = null;
    this.approvalComment = '';
    this.cdr.markForCheck();
  }

  confirmApproval(): void {
    if (!this.currentDisbursalIdForApproval) return;

    const comment = this.approvalComment.trim() || 'Approved by Bank Admin';

    this.bankAdminService
      .approveSalaryRequest(this.currentDisbursalIdForApproval, 'APPROVE', comment)
      .subscribe({
        next: () => {
          this.toastService.show('Salary request approved successfully!', 'success');
          this.closeApprovalModal();
          this.loadDashboardData();
        },
        error: (error) => {
          console.error('❌ Error approving salary request:', error);

          if (error.status === 200) {
            this.toastService.show('Salary request approved successfully!', 'success');
            this.closeApprovalModal();
            this.loadDashboardData();
            return;
          }

          let errorMessage = 'Failed to approve salary request.';

          if (error.error && typeof error.error === 'string') {
            errorMessage = error.error.replace('Error: ', '');
          } else if (error.error?.error) {
            errorMessage = error.error.error;
          }

          this.toastService.show(errorMessage, 'error');
          this.closeApprovalModal();
        },
      });
  }

  // ================= Salary Rejection Modal =================
  rejectSalaryRequest(disbursalId: number): void {
    this.currentDisbursalIdForRejection = disbursalId;
    this.salaryRejectionReason = '';
    this.isSalaryDetailsModalOpen = false;
    this.isRejectSalaryModalOpen = true;
    this.cdr.markForCheck();
  }

  closeRejectSalaryModal(): void {
    this.isRejectSalaryModalOpen = false;
    this.currentDisbursalIdForRejection = null;
    this.salaryRejectionReason = '';
    this.cdr.markForCheck();
  }

  confirmSalaryRejection(): void {
    if (!this.salaryRejectionReason.trim()) {
      this.toastService.show('Rejection reason is required.', 'error');
      return;
    }

    if (!this.currentDisbursalIdForRejection) return;

    this.bankAdminService
      .approveSalaryRequest(
        this.currentDisbursalIdForRejection,
        'REJECT',
        this.salaryRejectionReason
      )
      .subscribe({
        next: () => {
          this.toastService.show('Salary request rejected successfully!', 'success');
          this.closeRejectSalaryModal();
          this.loadDashboardData();
        },
        error: (error) => {
          console.error('Error rejecting salary request:', error);
          this.toastService.show('Failed to reject salary request.', 'error');
        },
      });
  }

  // ================= Delete Organization =================
  deleteOrg(orgId: number): void {
    this.confirmService.confirm({
      title: 'Delete Organization',
      message: 'Are you sure you want to delete this organization? This action cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel',
      confirmCallback: () => {
        const reason = prompt('Please provide a reason for deletion:');
        if (!reason || !reason.trim()) {
          this.toastService.show('Deletion reason is required.', 'error');
          return;
        }

        this.bankAdminService.handleDeletionRequest(orgId, true, reason).subscribe({
          next: () => {
            this.toastService.show('Organization deleted successfully!', 'success');
            this.loadDashboardData();
            this.cdr.markForCheck();
          },
          error: (error) => {
            console.error('Error deleting organization:', error);
            this.toastService.show('Failed to delete organization. Please try again.', 'error');
          },
        });
      },
    });
  }

  // ================= Update Org =================
  updateorg(): void {
    this.toastService.show('Update organization feature coming soon!', 'success');
  }

  // ================= Messages =================
  markAsRead(messageId: number): void {
    this.bankAdminService.markMessageAsRead(messageId).subscribe({
      next: () => {
        this.toastService.show('Message marked as read!', 'success');
        this.loadDashboardData();
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('Error marking message as read:', error);
        this.toastService.show('Failed to mark message as read. Please try again.', 'error');
      },
    });
  }

  openPaymentDetailsModal(request: VendorPaymentRequest): void {
    this.selectedPaymentRequest = request;
    this.isPaymentDetailsModalOpen = true;
    this.cdr.markForCheck();
  }

  closePaymentDetailsModal(): void {
    this.isPaymentDetailsModalOpen = false;
    this.selectedPaymentRequest = null;
    this.cdr.markForCheck();
  }

  approvePaymentRequest(paymentId: number): void {
    this.closePaymentDetailsModal();

    this.confirmService.confirm({
      title: 'Approve Payment Request',
      message: 'Are you sure you want to approve this payment request?',
      confirmText: 'Yes, Approve',
      cancelText: 'Cancel',
      confirmCallback: () => {
        this.bankAdminService.approvePaymentRequest(paymentId, 'Approved by Bank Admin').subscribe({
          next: () => {
            this.toastService.show('Payment request approved successfully!', 'success');
            this.loadDashboardData();
          },
          error: (error) => {
            console.error('Error approving payment:', error);
            this.toastService.show('Failed to approve payment request', 'error');
          },
        });
      },
    });
  }

  rejectPaymentRequest(paymentId: number): void {
    this.currentPaymentIdForAction = paymentId;
    this.paymentRejectionReason = '';
    this.closePaymentDetailsModal();
    this.isPaymentRejectionModalOpen = true;
    this.cdr.markForCheck();
  }

  closePaymentApprovalModal(): void {
    this.isPaymentApprovalModalOpen = false;
    this.currentPaymentIdForAction = null;
    this.paymentApprovalComment = '';
    this.cdr.markForCheck();
  }

  confirmPaymentApproval(): void {
    if (!this.currentPaymentIdForAction) return;

    const comment = this.paymentApprovalComment.trim() || 'Approved by Bank Admin';

    this.bankAdminService.approvePaymentRequest(this.currentPaymentIdForAction, comment).subscribe({
      next: () => {
        this.toastService.show('Payment request approved successfully!', 'success');
        this.closePaymentApprovalModal();
        this.loadDashboardData();
      },
      error: (error) => {
        console.error('Error approving payment request:', error);
        this.toastService.show('Failed to approve payment request.', 'error');
      },
    });
  }

  closePaymentRejectionModal(): void {
    this.isPaymentRejectionModalOpen = false;
    this.currentPaymentIdForAction = null;
    this.paymentRejectionReason = '';
    this.cdr.markForCheck();
  }

  confirmPaymentRejection(): void {
    if (!this.paymentRejectionReason.trim()) {
      this.toastService.show('Rejection reason is required.', 'error');
      return;
    }

    if (!this.currentPaymentIdForAction) return;

    this.bankAdminService
      .rejectPaymentRequest(this.currentPaymentIdForAction, this.paymentRejectionReason)
      .subscribe({
        next: () => {
          this.toastService.show('Payment request rejected successfully!', 'success');
          this.closePaymentRejectionModal();
          this.loadDashboardData();
        },
        error: (error) => {
          console.error('Error rejecting payment request:', error);
          this.toastService.show('Failed to reject payment request.', 'error');
        },
      });
  }

  // ================= Logout =================
  logout(): void {
    this.confirmService.confirm({
      title: 'Logout',
      message: 'Are you sure you want to logout?',
      confirmText: 'Logout',
      cancelText: 'Cancel',
      confirmCallback: () => {
        this.authservices.logout();
      },
    });
  }
}
