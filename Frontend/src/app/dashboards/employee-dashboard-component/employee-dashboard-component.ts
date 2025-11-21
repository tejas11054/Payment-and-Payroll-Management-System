import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EmployeeDashboardService } from '../../services/employeeServices/employee-dashboard-service';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToastService } from '../../services/notification/toast-service';
import { ConfirmationConfigModal } from '../../services/ConfirmationConfig/confirmation-config-modal';
import { LoginAuthentication } from '../../services/login/login-authentication';


@Component({
  selector: 'app-employee-dashboard-component',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './employee-dashboard-component.html',
  styleUrl: './employee-dashboard-component.css',
})
export class EmployeeDashboardComponent implements OnInit {
  // Tab navigation
  activeTab: string = 'home';
  
  // Employee data
  employeeData: any = null;
  empId!: number;
  
  // Stats
  stats: any = {
    totalEarnings: 0,
    deductions: 0,
    netSalary: 0,
    pendingConcerns: 0,
  };
  
  // Salary slips
  salarySlips: any[] = [];
  showSlipPreview: boolean = false;
  previewSlipData: any = null;
  
  // Concerns
  concerns: any[] = [];
  pendingConcerns: number = 0;
  concernForm = {
    description: '',
    empid: 0,
  };
  selectedFile: File | null = null;
  
  // UI state
  isLoading: boolean = false;
  isDarkMode: boolean = false;
  downloadingSlipId: number | null = null; // ✅ Track which slip is downloading


  // Services
  dashboardService = inject(EmployeeDashboardService);
  authService = inject(LoginAuthentication);
  router = inject(Router);
  toastService = inject(ToastService);
  confirmService = inject(ConfirmationConfigModal);


  ngOnInit(): void {
    this.isDarkMode = localStorage.getItem('darkMode') === 'true';
    this.applyDarkMode();
    this.loadInitialData();
  }


  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('darkMode', this.isDarkMode.toString());
    this.applyDarkMode();
  }


  private applyDarkMode(): void {
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark-mode');
    } else {
      document.documentElement.classList.remove('dark-mode');
    }
  }


  loadInitialData(): void {
    const empId = this.authService.getEmpIdFromToken();
    if (!empId) {
      this.toastService.show('Session expired. Please log in again.', 'error');
      this.router.navigate(['/login']);
      return;
    }
    
    this.empId = empId;
    this.concernForm.empid = empId;
    
    this.loadEmployeeData();
    this.loadSalarySlips();
    this.loadPendingConcerns();
  }


  loadEmployeeData(): void {
    this.isLoading = true;
    this.dashboardService.getEmployeeData().subscribe({
      next: (data) => {
        this.employeeData = data;
        this.calculateStats();
        this.isLoading = false;
      },
      error: (error) => {
        this.toastService.show('Failed to load employee data', 'error');
        this.isLoading = false;
      },
    });
  }


  loadSalarySlips(): void {
    this.dashboardService.getSalarySlips().subscribe({
      next: (slips) => { 
        this.salarySlips = slips;
        console.log('✅ Loaded salary slips:', slips.length);
      },
      error: (error) => { 
        console.error('❌ Error loading salary slips:', error);
        this.toastService.show('Failed to load salary slips', 'error'); 
      },
    });
  }


  loadPendingConcerns(): void {
    this.dashboardService.getPendingConcernsCount(this.empId).subscribe({
      next: (count: number) => {
        this.pendingConcerns = count;
        this.stats.pendingConcerns = count;
      },
      error: (err: any) => { console.error('Error loading pending concerns:', err); }
    });
  }


  calculateStats(): void {
    if (this.employeeData) {
      const totalEarnings =
        (this.employeeData.basicSalary || 0) +
        (this.employeeData.hra || 0) +
        (this.employeeData.da || 0) +
        (this.employeeData.allowances || 0);

      const deductions = this.employeeData.pf || 0;
      this.stats.totalEarnings = totalEarnings;
      this.stats.deductions = deductions;
      this.stats.netSalary = totalEarnings - deductions;
    }
  }


  calculateNetSalary(): number {
    if (!this.employeeData) return 0;
    return this.stats.netSalary;
  }
  
  getInitials(): string {
    if (!this.employeeData?.empName) return 'U';
    return this.employeeData.empName
      .split(' ')
      .map((n: string) => n[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }


  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }


  previewSlip(slipId: number): void {
    console.log('📄 Opening preview for slip ID:', slipId);
    this.isLoading = true;
    
    this.dashboardService.getSalarySlipDetails(slipId).subscribe({
      next: (data) => {
        console.log('✅ Preview data received:', data);
        this.previewSlipData = data;
        this.showSlipPreview = true;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ Error loading preview:', error);
        this.toastService.show('Failed to load salary slip details', 'error');
        this.isLoading = false;
      },
    });
  }


  closePreview(): void {
    this.showSlipPreview = false;
    this.previewSlipData = null;
  }


  // ✅ FIXED: Download with proper Angular change detection
  downloadSlip(slipId: number): void {
    console.log('📥 ════════════════════════════════════════');
    console.log('📥 STARTING DOWNLOAD');
    console.log('📥 ════════════════════════════════════════');
    console.log('   Slip ID:', slipId);
    
    // Check if already downloading this slip
    if (this.downloadingSlipId === slipId) {
      this.toastService.show('Download already in progress...', 'warning');
      return;
    }

    // ✅ Use setTimeout to avoid ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => {
      this.downloadingSlipId = slipId;
    });
    
    console.log('✅ Download state set for slip:', slipId);
    this.toastService.show('Preparing download...', 'info');

    this.dashboardService.downloadSalarySlip(slipId).subscribe({
      next: (blob: Blob) => {
        console.log('✅ PDF blob received');
        console.log('   Size:', blob.size, 'bytes');
        console.log('   Type:', blob.type);
        
        // Check if blob is valid
        if (!blob || blob.size === 0) {
          console.error('❌ Empty or invalid blob received');
          this.toastService.show('Empty file received from server', 'error');
          setTimeout(() => {
            this.downloadingSlipId = null;
          });
          return;
        }

        try {
          // Create download link
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `salary-slip-${slipId}.pdf`;
          a.style.display = 'none';
          
          // Trigger download
          document.body.appendChild(a);
          a.click();
          
          console.log('✅ Download triggered successfully');
          
          // Cleanup
          setTimeout(() => {
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            console.log('✅ Cleanup completed');
          }, 100);

          this.toastService.show('✅ Salary slip downloaded successfully!', 'success');
          
        } catch (error) {
          console.error('❌ Error during download process:', error);
          this.toastService.show('Error creating download link', 'error');
        } finally {
          // ✅ Reset state after delay
          setTimeout(() => {
            this.downloadingSlipId = null;
            console.log('✅ Download state reset');
            console.log('📥 ════════════════════════════════════════\n');
          });
        }
      },
      error: (error) => {
        console.error('❌ ════════════════════════════════════════');
        console.error('❌ DOWNLOAD ERROR');
        console.error('❌ ════════════════════════════════════════');
        console.error('   Error:', error);
        console.error('   Status:', error.status);
        console.error('   Message:', error.message);
        
        let errorMessage = 'Failed to download salary slip';
        
        if (error.status === 404) {
          errorMessage = 'Salary slip not found';
        } else if (error.status === 500) {
          errorMessage = 'Server error while generating PDF';
        } else if (error.status === 0) {
          errorMessage = 'Network error. Please check your connection.';
        }
        
        this.toastService.show(errorMessage, 'error');
        
        // ✅ Reset state after delay
        setTimeout(() => {
          this.downloadingSlipId = null;
          console.log('✅ Download state reset (after error)');
          console.error('❌ ════════════════════════════════════════\n');
        });
      },
    });
  }

  // ✅ Helper method to check if downloading
  isDownloading(slipId: number): boolean {
    return this.downloadingSlipId === slipId;
  }
  
  onFileSelect(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        this.toastService.show('File size should not exceed 5MB', 'error');
        return;
      }
      this.selectedFile = file;
    }
  }


  submitConcern(): void {
    if (!this.concernForm.description.trim()) {
      this.toastService.show('Please provide a description for your concern.', 'error');
      return;
    }

    this.isLoading = true;
    const formData = new FormData();
    const concernDTO = {
      description: this.concernForm.description,
      empid: this.concernForm.empid
    };

    formData.append('data', new Blob([JSON.stringify(concernDTO)], { type: 'application/json' }));
    
    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }

    this.dashboardService.raiseConcern(formData).subscribe({
      next: () => {
        this.toastService.show('Concern raised successfully!', 'success');
        this.concernForm.description = '';
        this.selectedFile = null;
        this.loadPendingConcerns();
        this.isLoading = false;
      },
      error: (error) => {
        this.toastService.show('Failed to raise concern.', 'error');
        this.isLoading = false;
      },
    });
  }


  logout(): void {
    this.confirmService.confirm({
      title: 'Logout',
      message: 'Are you sure you want to logout?',
      confirmText: 'Logout',
      cancelText: 'Cancel',
      confirmCallback: () => {
        localStorage.removeItem('authToken');
        this.toastService.show('Logged out successfully', 'success');
        this.router.navigate(['/login']);
      },
    });
  }
}
