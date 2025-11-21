import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Employee, OrgAdmin, PayrollService } from '../../services/payroll/payroll-service';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToastService } from '../../services/notification/toast-service';

import { Router } from '@angular/router';
import { ConfirmationConfigModal } from '../../services/ConfirmationConfig/confirmation-config-modal';
import { LoginAuthentication } from '../../services/login/login-authentication';

interface SelectedEmployee extends Employee {
  selected: boolean;
  netSalary?: number;
  showDetails?: boolean; 
}

interface SelectedOrgAdmin extends OrgAdmin {
  selected: boolean;
  netSalary?: number;
  showDetails?: boolean; 
  gradeCode?: string;  
}
@Component({
  selector: 'app-payroll-component',
  standalone: true, // Assuming standalone
  imports: [CommonModule, FormsModule,ReactiveFormsModule],
  templateUrl: './payroll-component.html',
  styleUrl: './payroll-component.css'
})
export class PayrollComponent  implements OnInit{
 // Services
 payrollService = inject(PayrollService);
 toastService = inject(ToastService);
 confirmService = inject(ConfirmationConfigModal);
 router = inject(Router);
   authService = inject(LoginAuthentication);
 cdr = inject(ChangeDetectorRef);
 
 // Data
 employees: SelectedEmployee[] = [];
 orgAdmins: SelectedOrgAdmin[] = [];
 
 // State
 isLoading = false;
 showPreview = false;
 currentStep = 1; // 1: Selection, 2: Preview, 3: Confirmation
 
 // Form data
 orgId: number = 0;
 period: string = '';
 remarks: string = '';
 
 // Filters
 searchTerm: string = '';
 selectedDepartment: string = 'ALL';
 departments: string[] = [];
 
 // Calculations
 totalGross = 0;
 totalDeductions = 0;
 totalNet = 0;
 selectedCount = 0;

 
 ngOnInit(): void {
   console.log('🚀 PayrollComponent initialized');
   
   // ✅ Get orgId from JWT token
   this.orgId = this.authService.getOrgIdFromToken() || 0;
   
   console.log('🔐 OrgId from JWT:', this.orgId);
   
   // ✅ Validate orgId
   if (!this.orgId || this.orgId === 0 || isNaN(this.orgId)) {
     console.error('❌ Invalid orgId:', this.orgId);
     this.toastService.show('Organization not found. Please login again.', 'error');
     
     setTimeout(() => {
       this.authService.logout();
       this.router.navigate(['/login']);
     }, 2000);
     
     return;
   }
   
   console.log('✅ Valid orgId:', this.orgId);
   
   // Set default period (current month)
   const now = new Date();
   this.period = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
   
   console.log('📅 Period set to:', this.period);
   
   this.loadEmployeesAndAdmins();
 }


loadEmployeesAndAdmins(): void {
    this.isLoading = true;
    
    console.log('📡 Loading employees and admins for orgId:', this.orgId);

    // ✅ Load employees - use orgId as number
    this.payrollService.getOrganizationEmployees(this.orgId).subscribe({
      next: (employees) => {
        console.log('✅ Employees loaded:', employees.length);
        
        this.employees = employees.map(emp => ({
          ...emp,
          selected: false,
          netSalary: this.calculateNetSalary(emp.salaryGrade)
        }));

        // Extract unique departments
        this.departments = ['ALL', ...new Set(employees
          .map(e => e.departmentName)
          .filter(d => d) as string[])];

        console.log('🏢 Departments:', this.departments);
        this.isLoading = false;
        
        // ✅ FIX: Manually trigger change detection after data is loaded
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error loading employees:', error);
        this.toastService.show('Failed to load employees', 'error');
        this.isLoading = false;
        
        // ✅ FIX: Also trigger change detection on error
        this.cdr.detectChanges();
      }
    });

    // ✅ Load org admins - use orgId as number
    this.payrollService.getOrganizationAdmins(this.orgId).subscribe({
      next: (admins) => {
        console.log('✅ Org admins loaded:', admins.length);
        
        this.orgAdmins = admins.map(admin => ({
          ...admin,
          selected: false,
          netSalary: this.calculateNetSalary(admin.salaryGrade)
        }));
        
        // ✅ FIX: Manually trigger change detection after data is loaded
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error loading org admins:', error);
        // Don't show error toast - admins might not exist
        this.orgAdmins = [];
        
        // ✅ FIX: Also trigger change detection on error
        this.cdr.detectChanges();
      }
    });
 }

 // ... (rest of your component code remains the same)
 calculateNetSalary(grade: any): number {
  if (!grade) {
    console.warn('⚠️ No salary grade provided');
    return 0;
  }
  
  const basic = Number(grade.basicSalary) || 0;
  const hra = Number(grade.hra) || 0;
  const da = Number(grade.da) || 0;
  const allowances = Number(grade.allowances) || 0;
  const pf = Number(grade.pf) || 0;
  
  const gross = basic + hra + da + allowances;
  const net = gross - pf;
  
  console.log(`💰 Calculated Net Salary: ${net} (Gross: ${gross} - PF: ${pf})`);
  
  return net;
}

toggleEmployee(employee: SelectedEmployee): void {
  employee.selected = !employee.selected;
  
  console.log(`🔘 Employee ${employee.empName} ${employee.selected ? 'selected' : 'deselected'}`);
  console.log('Salary Grade:', employee.salaryGrade);
  
  this.updateCalculations();
  
  console.log('📊 Updated calculations:', {
    selectedCount: this.selectedCount,
    totalGross: this.totalGross,
    totalDeductions: this.totalDeductions,
    totalNet: this.totalNet
  });
}

toggleOrgAdmin(admin: SelectedOrgAdmin): void {
  admin.selected = !admin.selected;
  this.updateCalculations();
}

selectAllEmployees(): void {
  const filtered = this.getFilteredEmployees();
  const allSelected = filtered.every(e => e.selected);
  
  filtered.forEach(emp => {
    emp.selected = !allSelected;
  });
  
  this.updateCalculations();
}

selectAllAdmins(): void {
  const allSelected = this.orgAdmins.every(a => a.selected);
  
  this.orgAdmins.forEach(admin => {
    admin.selected = !allSelected;
  });
  
  this.updateCalculations();
  this.cdr.markForCheck();
}

updateCalculations(): void {
  this.totalGross = 0;
  this.totalDeductions = 0;
  this.totalNet = 0;
  this.selectedCount = 0;

  console.log('🔄 Updating calculations...');

  // Calculate for employees
  const selectedEmployees = this.employees.filter(e => e.selected);
  console.log(`📋 Selected employees: ${selectedEmployees.length}`);
  
  selectedEmployees.forEach(emp => {
    if (emp.salaryGrade) {
      const basic = Number(emp.salaryGrade.basicSalary) || 0;
      const hra = Number(emp.salaryGrade.hra) || 0;
      const da = Number(emp.salaryGrade.da) || 0;
      const allowances = Number(emp.salaryGrade.allowances) || 0;
      const pf = Number(emp.salaryGrade.pf) || 0;
      
      const gross = basic + hra + da + allowances;
      const deductions = pf;
      const net = gross - deductions;
      
      console.log(`✅ Employee: ${emp.empName}, Net: ${net}`);
      
      this.totalGross += gross;
      this.totalDeductions += deductions;
      this.totalNet += net;
      this.selectedCount++;
    } else {
      console.warn(`⚠️ Employee ${emp.empName} has no salary grade!`);
    }
  });

  // Calculate for org admins
  const selectedAdmins = this.orgAdmins.filter(a => a.selected);
  console.log(`👥 Selected admins: ${selectedAdmins.length}`);
  
  selectedAdmins.forEach(admin => {
    if (admin.salaryGrade) {
      const basic = Number(admin.salaryGrade.basicSalary) || 0;
      const hra = Number(admin.salaryGrade.hra) || 0;
      const da = Number(admin.salaryGrade.da) || 0;
      const allowances = Number(admin.salaryGrade.allowances) || 0;
      const pf = Number(admin.salaryGrade.pf) || 0;
      
      const gross = basic + hra + da + allowances;
      const deductions = pf;
      const net = gross - deductions;
      
      console.log(`✅ Admin: ${admin.name}, Net: ${net}`);
      
      this.totalGross += gross;
      this.totalDeductions += deductions;
      this.totalNet += net;
      this.selectedCount++;
    } else {
      console.warn(`⚠️ Admin ${admin.name} has no salary grade!`);
    }
  });

  console.log('📊 Final calculations:', {
    selectedCount: this.selectedCount,
    totalGross: this.totalGross,
    totalDeductions: this.totalDeductions,
    totalNet: this.totalNet
  });
}
testCalculations(): void {
  console.log('=== MANUAL TEST ===');
  console.log('Total employees:', this.employees.length);
  console.log('Total admins:', this.orgAdmins.length);
  
  this.employees.forEach((emp, index) => {
    console.log(`Employee ${index + 1}:`, {
      name: emp.empName,
      selected: emp.selected,
      hasSalaryGrade: !!emp.salaryGrade,
      salaryGrade: emp.salaryGrade
    });
  });
  
  this.updateCalculations();
  
  console.log('After calculation:', {
    selectedCount: this.selectedCount,
    totalNet: this.totalNet
  });
}
getFilteredEmployees(): SelectedEmployee[] {
  return this.employees.filter(emp => {
    const matchesSearch = !this.searchTerm || 
      emp.empName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      emp.empEmail.toLowerCase().includes(this.searchTerm.toLowerCase());
    
    const matchesDepartment = this.selectedDepartment === 'ALL' || 
      emp.departmentName === this.selectedDepartment;
    
    return matchesSearch && matchesDepartment;
    
  });
  
}

goToPreview(): void {
  if (this.selectedCount === 0) {
    this.toastService.show('Please select at least one employee or admin', 'error');
    return;
  }

  if (!this.period) {
    this.toastService.show('Please select a period', 'error');
    return;
  }

  this.currentStep = 2;
}

backToSelection(): void {
  this.currentStep = 1;
}

submitRequest(): void {
  this.confirmService.confirm({
    title: 'Confirm Salary Disbursal Request',
    message: `Are you sure you want to submit salary disbursal request for ${this.selectedCount} people with total amount ₹${this.totalNet.toFixed(2)}?`,
    confirmText: 'Submit Request',
    cancelText: 'Cancel',
    confirmCallback: () => {
      this.processSubmission();
    }
  });
}

// Only showing the updated processSubmission() method and error handling

// payroll-component.ts

private processSubmission(): void {
  this.isLoading = true;
  
  console.log('🔄 isLoading set to true');

  const selectedEmployeeIds = this.employees
    .filter(e => e.selected)
    .map(e => e.empId);

  const selectedAdminIds = this.orgAdmins
    .filter(a => a.selected)
    .map(a => a.orgAdminId);

  const request: any = {
    orgId: this.orgId,
    period: this.period,
    remarks: this.remarks || 'Salary disbursal request',
    payments: []
  };

  if (selectedEmployeeIds.length > 0) {
    request.payments.push({
      type: 'ROLE_EMPLOYEE',
      ids: selectedEmployeeIds
    });
  }

  if (selectedAdminIds.length > 0) {
    request.payments.push({
      type: 'ROLE_ORG_ADMIN',
      ids: selectedAdminIds
    });
  }

  console.log('📤 Submitting salary disbursal request:', request);

  this.payrollService.createSalaryDisbursalRequest(request).subscribe({
    next: (response) => {
      console.log('✅ Salary disbursal request created:', response);
      
      this.isLoading = false;
      console.log('🔄 isLoading set to false (success)');
      
      this.toastService.show(
        `Salary disbursal request submitted successfully! Request ID: ${response.disbursalId}`,
        'success'
      );
      
      setTimeout(() => {
        this.router.navigate(['/organization/dashboard']);
      }, 2000);
    },
    error: (error: any) => {
      console.error('❌ Error creating salary disbursal request:', error);
      
      // ✅ CRITICAL: Always stop loading on error
      this.isLoading = false;
      console.log('🔄 isLoading set to false (error)');
      
      // ✅ Trigger change detection
      this.cdr.detectChanges();
      
      // ✅ UPDATED: Better error message extraction
      let errorMessage = 'Failed to submit salary disbursal request';
      
      // Extract message from error object
      if (error.message) {
        errorMessage = error.message;
      } else if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (typeof error === 'string') {
        errorMessage = error;
      }
      
      console.log('📝 Displaying error message:', errorMessage);
      
      // ✅ Handle different error types
      if (error.type === 'DUPLICATE_PERIOD') {
        // Show warning toast
        this.toastService.show(errorMessage, 'warning');
        
        // Optional: Show confirmation dialog
        this.confirmService.confirm({
          title: '⚠️ Duplicate Payroll Request',
          message: errorMessage,
          confirmText: 'Go to Dashboard',
          cancelText: 'Close',
          confirmCallback: () => {
            this.router.navigate(['/organization/dashboard']);
          }
        });
      } else {
        // Show error toast
        this.toastService.show(errorMessage, 'error');
      }
    },
    complete: () => {
      console.log('🔄 Request completed');
      if (this.isLoading) {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    }
  });
}


// ✅ Updated: Better error handler
private handleDuplicatePeriodError(error: any): void {
  console.warn('⚠️ Duplicate period error:', error);
  
  // ✅ Show warning toast with proper message
  const message = error.message || 
    `A salary disbursal request already exists for period ${error.period}. Please wait for approval or contact administrator.`;
  
  this.toastService.show(message, 'warning');
  

}



areAllEmployeesSelected(): boolean {
  const filtered = this.getFilteredEmployees();
  return filtered.length > 0 && filtered.every(e => e.selected);
}

areAllAdminsSelected(): boolean {
  return this.orgAdmins.length > 0 && this.orgAdmins.every(a => a.selected);
}

getSelectedEmployees(): SelectedEmployee[] {
  return this.employees.filter(e => e.selected);
}

getSelectedAdmins(): SelectedOrgAdmin[] {
  return this.orgAdmins.filter(a => a.selected);
}
toggleEmployeeDetails(employee: SelectedEmployee): void {
  employee.showDetails = !employee.showDetails;
}

toggleAdminDetails(admin: SelectedOrgAdmin): void {
  admin.showDetails = !admin.showDetails;
}
cancel(): void {
  this.confirmService.confirm({
    title: 'Cancel Payroll',
    message: 'Are you sure you want to cancel? All selections will be lost.',
    confirmText: 'Yes, Cancel',
    cancelText: 'No, Continue',
    confirmCallback: () => {
      this.router.navigate(['/organization/dashboard']);
    }
    
  });
}


}
