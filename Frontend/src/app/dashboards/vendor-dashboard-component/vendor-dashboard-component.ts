import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastService } from '../../services/notification/toast-service';
import { ConfirmationConfigModal } from '../../services/ConfirmationConfig/confirmation-config-modal';
import { VendorService } from '../../services/vendor/vendor';
import { LoginAuthentication } from '../../services/login/login-authentication';

interface VendorProfile {
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

interface PaymentReceipt {
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

@Component({
  selector: 'app-vendor-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './vendor-dashboard-component.html',
  styleUrls: ['./vendor-dashboard-component.css']
})
export class VendorDashboardComponent implements OnInit {
  
  // Services
  private vendorService = inject(VendorService);
  private toastService = inject(ToastService);
  private confirmService = inject(ConfirmationConfigModal);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private authService = inject(LoginAuthentication);
  private cdr = inject(ChangeDetectorRef);

  // State
  currentView: string = 'profile';
  isLoading: boolean = true;
  
  // Profile
  profile: VendorProfile | null = null;
  isEditingProfile: boolean = false;
  profileForm!: FormGroup;
  isDarkMode: boolean = false;
  // Receipts
  receipts: PaymentReceipt[] = [];
  selectedReceipt: PaymentReceipt | null = null;
  isReceiptModalOpen: boolean = false;
  
  // Password Change
  isPasswordModalOpen: boolean = false;
  passwordForm!: FormGroup;

  ngOnInit(): void {
    this.initializeForms();
    this.loadProfile();
    this.loadReceipts();
    const savedTheme = localStorage.getItem('theme');
  this.isDarkMode = savedTheme === 'dark';
  }

  // ═══════════════════════════════════════════════════════════════════
  // INITIALIZE FORMS
  // ═══════════════════════════════════════════════════════════════════
  initializeForms(): void {
    this.profileForm = this.fb.group({
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      bankName: [''],
      bankAccountNo: [''],
      ifscCode: [''],
      accountHolderName: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'mismatch': true };
  }

  // ═══════════════════════════════════════════════════════════════════
  // LOAD PROFILE
  // ═══════════════════════════════════════════════════════════════════
  loadProfile(): void {
    this.isLoading = true;
    this.vendorService.getMyProfile().subscribe({
      next: (data) => {
        this.profile = data;
        this.profileForm.patchValue(data);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.toastService.show('Failed to load profile', 'error');
        this.isLoading = false;
      }
    });
    this.cdr.detectChanges();
  }

  // ═══════════════════════════════════════════════════════════════════
  // UPDATE PROFILE
  // ═══════════════════════════════════════════════════════════════════
  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.toastService.show('Please fill all required fields correctly', 'error');
      return;
    }

    this.vendorService.updateMyProfile(this.profileForm.value).subscribe({
      next: (data) => {
        this.profile = data;
        this.isEditingProfile = false;
        this.toastService.show('Profile updated successfully', 'success');
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        this.toastService.show('Failed to update profile', 'error');
      }
    });
  }

  cancelEdit(): void {
    this.isEditingProfile = false;
    if (this.profile) {
      this.profileForm.patchValue(this.profile);
    }
  }

  // ═══════════════════════════════════════════════════════════════════
  // LOAD RECEIPTS
  // ═══════════════════════════════════════════════════════════════════
  loadReceipts(): void {
    this.vendorService.getMyReceipts().subscribe({
      next: (data) => {
        this.receipts = data;
      },
      error: (error) => {
        console.error('Error loading receipts:', error);
        this.toastService.show('Failed to load receipts', 'error');
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // VIEW RECEIPT
  // ═══════════════════════════════════════════════════════════════════
  viewReceipt(receipt: PaymentReceipt): void {
    this.selectedReceipt = receipt;
    this.isReceiptModalOpen = true;
  }

  closeReceiptModal(): void {
    this.isReceiptModalOpen = false;
    this.selectedReceipt = null;
  }

  // ═══════════════════════════════════════════════════════════════════
  // DOWNLOAD RECEIPT
  // ═══════════════════════════════════════════════════════════════════
  downloadReceipt(receiptId: number): void {
    this.closeReceiptModal();
    
    this.confirmService.confirm({
      title: 'Download Receipt',
      message: 'Are you sure you want to download this receipt?',
      confirmText: 'Download',
      cancelText: 'Cancel',
      confirmCallback: () => {
        this.vendorService.downloadReceipt(receiptId).subscribe({
          next: (data) => {
            const blob = new Blob([data], { type: 'text/plain' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `receipt_${receiptId}.txt`;
            link.click();
            window.URL.revokeObjectURL(url);
            this.toastService.show('Receipt downloaded successfully', 'success');
          },
          error: (error) => {
            console.error('Error downloading receipt:', error);
            this.toastService.show('Failed to download receipt', 'error');
          }
        });
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // CHANGE PASSWORD
  // ═══════════════════════════════════════════════════════════════════
  openPasswordModal(): void {
    this.passwordForm.reset();
    this.isPasswordModalOpen = true;
  }

  closePasswordModal(): void {
    this.isPasswordModalOpen = false;
    this.passwordForm.reset();
  }

  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.toastService.show('Please fill all fields correctly', 'error');
      return;
    }

    this.vendorService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.toastService.show('Password changed successfully', 'success');
        this.closePasswordModal();
      },
      error: (error) => {
        console.error('Error changing password:', error);
        const message = error.error || 'Failed to change password';
        this.toastService.show(message, 'error');
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  // VIEW SWITCHING
  // ═══════════════════════════════════════════════════════════════════
  setView(view: string): void {
    this.currentView = view;
  }

  // ═══════════════════════════════════════════════════════════════════
  // LOGOUT
  // ═══════════════════════════════════════════════════════════════════
  logout(): void {
    this.confirmService.confirm({
      title: 'Logout',
      message: 'Are you sure you want to logout?',
      confirmText: 'Logout',
      cancelText: 'Cancel',
      confirmCallback: () => {
    this.authService.logout();
        this.router.navigate(['/login']);
      }
    });
  }
}

