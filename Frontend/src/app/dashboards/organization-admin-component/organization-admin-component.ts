import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

// Services
import { LoginAuthentication } from '../../services/login/login-authentication';
import { ToastService } from '../../services/notification/toast-service';
import { OrgAuthservice } from '../../services/orgDashboard/org-authservice';
import { Department } from '../../services/departments/department';
import { OrgAdminService } from '../../services/orgAdmin/org-admin-service';
import { Payment } from '../../services/payment/payment';
import {
  EmployeeDTO,
  EmployeeRequestDTO,
  EmployeeResponseDTO,
} from "../../EmployeeDTO's/EmployeeDTO";
import {
  DepartmentResponseDTO,
  OrgAdminRequestDTO,
  OrgAdminResponseDTO,
} from "../../DTO's/DepartmentDTO";
import {
  SalaryGrade,
  SalaryGradeRequestDTO,
  SalaryGradeResponseDTO,
} from '../../services/salaryGrade/salary-grade';
import { HttpErrorResponse } from '@angular/common/http';
import { NotificationComponent } from '../../components/notification-component/notification-component';
import { VendorManagementComponent } from '../../components/vendor-management/vendor-management';
import { ConfirmationConfigModal } from '../../services/ConfirmationConfig/confirmation-config-modal';

// Custom Validator
export const passwordMatchValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const newPassword = control.get('newPassword');
  const confirmPassword = control.get('confirmPassword');
  return newPassword && confirmPassword && newPassword.value !== confirmPassword.value
    ? { passwordMismatch: true }
    : null;
};

@Component({
  selector: 'app-organization-admin-component',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    NotificationComponent,
    FormsModule,
    VendorManagementComponent,
  ],
  templateUrl: './organization-admin-component.html',
  styleUrls: ['./organization-admin-component.css'],
})
export class OrganizationAdminComponent implements OnInit {
  // --- Services ---
  private authService = inject(LoginAuthentication);
  private toastService = inject(ToastService);
  private employeeService = inject(OrgAuthservice);
  private departmentService = inject(Department);
  private orgAdminService = inject(OrgAdminService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);
 confirmService = inject(ConfirmationConfigModal);
  private salaryGradeService = inject(SalaryGrade);

  // --- User Profile State ---
  loggedInUserName: string | null = null;
  loggedInUserEmail: string | null = null;
  loggedInUserRoles: string[] = [];
  organizationName: string | null = null;
  accountBalance: number = 0;

  // --- Component State ---
  orgStatus: 'PENDING' | 'APPROVED' | 'REJECTED' | 'UNKNOWN' = 'UNKNOWN';
  isLoading = true;
  currentView: 'employees' | 'departments' | 'admins' | 'salary-grades' | 'vendors' = 'employees';

  // Data arrays
  employees: EmployeeDTO[] = [];
  departments: DepartmentResponseDTO[] = [];
  admins: OrgAdminResponseDTO[] = [];

  stats = { totalEmployees: 0, totalDepartments: 0, totalAdmins: 0 };
  loggedInEmail: string | null = null;

  // --- Pagination State ---
  paginatedEmployees: EmployeeDTO[] = [];
  paginatedDepartments: DepartmentResponseDTO[] = [];
  paginatedAdmins: OrgAdminResponseDTO[] = [];

  currentPage = 1;
  itemsPerPage = 4;
  totalPages = 0;

  // --- Modals & Forms ---
  isProfileModalOpen = false;
  isAddSingleEmployeeModalOpen = false;
  isEditEmployeeModalOpen = false;
  isBatchUploadModalOpen = false;
  isChangePasswordModalOpen = false;
  isDepartmentModalOpen = false;
  isOrgAdminModalOpen = false;
  isEditingDepartment = false;
  isEditingAdmin = false;
  isAddMoneyModalOpen = false;
  addMoneyAmount: number = 10000;
  private paymentService = inject(Payment);

  isLoadingBalance: boolean = false;

  profileForm!: FormGroup;
  addEmployeeForm!: FormGroup;
  editEmployeeForm!: FormGroup;
  batchUploadForm!: FormGroup;
  changePasswordForm!: FormGroup;
  departmentForm!: FormGroup;
  orgAdminForm!: FormGroup;

  batchFile: File | null = null;
  adminDocumentFile: File | null = null;
  selectedDepartmentId: undefined;

  salaryGrades: SalaryGradeResponseDTO[] = [];
  isSalaryGradeModalOpen = false;
  isEditingSalaryGrade = false;

  isOrgAdmin: boolean = false;
  isOrganization: boolean = false;
  userDepartmentName: string | null = null;

  constructor() {
    this.initializeForms();
  }
  salaryGradeForm = this.fb.group({
    gradeId: [null as number | null],
    gradeCode: ['', [Validators.required, Validators.pattern(/^[A-Z0-9-]+$/)]],
    basicSalary: [0, [Validators.required, Validators.min(0)]],
    hra: [0, [Validators.required, Validators.min(0)]],
    da: [0, [Validators.required, Validators.min(0)]],
    pf: [0, [Validators.required, Validators.min(0)]],
    allowances: [0, [Validators.required, Validators.min(0)]],
  });

  initializeForms(): void {
    this.addEmployeeForm = this.fb.group({
      empName: ['', Validators.required],
      empEmail: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      bankAccountName: ['', Validators.required], // ✅ NEW
      bankAccountNo: ['', Validators.required],
      ifscCode: ['', Validators.required],
      departmentName: [null as string | null, Validators.required],
      salaryGradeId: [null as number | null], // ✅ Use number for ID
      balance: [0],
    });

    this.editEmployeeForm = this.fb.group({
      empId: [null as number | null],
      empName: ['', Validators.required],
      empEmail: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      bankAccountName: ['', Validators.required],
      bankAccountNo: ['', Validators.required],
      ifscCode: ['', Validators.required],
      departmentName: [null as string | null, Validators.required],
      salaryGradeId: [null as number | null], // ✅ Use number for ID
      balance: [0],
    });

    this.changePasswordForm = this.fb.group(
      {
        currentPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: passwordMatchValidator }
    );

    this.departmentForm = this.fb.group({
      departmentId: [null],
      name: ['', Validators.required],
      description: [''],
    });

    this.orgAdminForm = this.fb.group({
      orgAdminId: [null],
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      departmentName: [null],
    });

    this.batchUploadForm = this.fb.group({
      departmentName: [null, Validators.required],
      salaryGradeId: [null], // ✅ NEW - Optional salary grade for bulk upload
    });

    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      phone: ['', Validators.required],
    });
  }
  orgId: number = 0;
  ngOnInit(): void {
    this.loggedInEmail = this.authService.getEmailFromToken();
    const orgId = this.authService.getOrgIdFromToken();
    this.loadUserProfile();
    this.loadOrganizationInfo();
    setTimeout(() => this.loadInitialData());
    this.currentView = 'employees';
    this.loadAccountBalance();
    // ✅ GET orgId

    if (orgId) {
      this.orgId = orgId;
    }
    // ✅ CHECK USER ROLE
    const roles = this.authService.getRoleFromToken();
    if (Array.isArray(roles)) {
      this.isOrgAdmin = roles.includes('ROLE_ORG_ADMIN');
      this.isOrganization = roles.includes('ROLE_ORGANIZATION');
    } else if (typeof roles === 'string') {
      this.isOrgAdmin = roles === 'ROLE_ORG_ADMIN';
      this.isOrganization = roles === 'ROLE_ORGANIZATION';
    }

    // ✅ Load org admin's department
    if (this.isOrgAdmin) {
      this.loadOrgAdminDepartment();
    }

    this.loadUserProfile();
    this.loadOrganizationInfo();
    setTimeout(() => this.loadInitialData());
    this.currentView = 'employees';
    this.loadAccountBalance();
  }
  private loadOrgAdminDepartment(): void {
    this.orgAdminService.getAdminsForCurrentOrg().subscribe({
      next: (admins) => {
        const currentAdmin = admins.find((admin) => admin.email === this.loggedInEmail);
        if (currentAdmin && currentAdmin.departmentName) {
          this.userDepartmentName = currentAdmin.departmentName;
          console.log('Org Admin Department:', this.userDepartmentName);
        }
      },
      error: (err) => {
        console.error('Failed to load admin department:', err);
      },
    });
  }

  loadUserProfile(): void {
    this.loggedInUserEmail = this.authService.getEmailFromToken();

    // Safely get name from token with fallback
    try {
      this.loggedInUserName =
        this.authService.getNameFromToken?.() || this.loggedInUserEmail?.split('@')[0] || 'Admin';
    } catch (error) {
      console.warn('getNameFromToken not implemented, using email as fallback');
      this.loggedInUserName = this.loggedInUserEmail?.split('@')[0] || 'Admin';
    }

    // Safely get roles from token with fallback
    try {
      const roles = this.authService.getRoleFromToken?.();
      if (typeof roles === 'string') {
        this.loggedInUserRoles = [roles];
      } else {
        this.loggedInUserRoles = roles ?? ['ADMIN'];
      }
    } catch (error) {
      console.warn('getRoleFromToken not implemented, using default role');
      this.loggedInUserRoles = ['ADMIN'];
    }
  }

  loadOrganizationInfo(): void {
    // Safely get organization name from token with fallback
    try {
      this.organizationName = this.authService.getOrgNameFromToken?.() || 'Organization';
    } catch (error) {
      console.warn('getOrgNameFromToken not implemented, using default');
      this.organizationName = 'My Organization';
    }

    // Fetch account balance
    this.loadAccountBalance();
  }
  loadAccountBalance(): void {
    this.isLoadingBalance = true;
    this.employeeService.getMyAccountBalance().subscribe({
      next: (balance) => {
        this.accountBalance = balance;
        this.isLoadingBalance = false;
      },
      error: (err) => {
        console.error('Failed to load account balance:', err);
        this.accountBalance = 0;
        this.isLoadingBalance = false;
      },
    });
    this.cdr.detectChanges();
  }

  private loadInitialData(): void {
    this.isLoading = true;
    this.orgStatus = this.authService.getOrgStatusFromToken();

    if (this.orgStatus === 'APPROVED') {
      // ✅ Load departments first, then salary grades
      this.loadAllDepartments().then(() => {
        // ✅ Load salary grades for BOTH roles (needed for employee forms)
        this.loadAllSalaryGrades();

        // Load admins only for organization role
        if (this.isOrganization) {
          this.loadAllAdmins();
        }

        this.loadAccountBalance();
        this.setView('employees');
      });
    } else {
      this.stopLoading();
    }
  }

  // --- View Switching ---
  setView(view: 'employees' | 'departments' | 'admins' | 'salary-grades' | 'vendors'): void {
    if (!this.canAccessView(view)) {
      this.toastService.show('You do not have permission to access this section', 'error');
      return;
    }

    this.currentView = view;

    switch (view) {
      case 'employees':
        this.loadAllEmployees();
        this.cdr.detectChanges();
        break;
      case 'departments':
        if (this.isOrganization) {
          this.loadAllDepartments();
          this.cdr.detectChanges();
        }
        break;
      case 'admins':
        if (this.isOrganization) {
          this.loadAllAdmins();
          this.cdr.detectChanges();
        }
        break;
      case 'salary-grades':
        if (this.isOrganization) {
          // ✅ Only ROLE_ORGANIZATION can access tab
          this.loadAllSalaryGrades();
          this.cdr.detectChanges();
        }
        break;
      case 'vendors': // ✅ ADD THIS CASE
        // Vendor component handles its own data loading
        this.cdr.detectChanges();
        break;
    }
    this.cdr.detectChanges();
  }

  updatePagination(): void {
    let data: any[] = [];
    if (this.currentView === 'employees') data = this.employees;
    else if (this.currentView === 'departments') data = this.departments;
    else if (this.currentView === 'admins') data = this.admins;

    this.totalPages = Math.ceil(data.length / this.itemsPerPage);
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;

    if (this.currentView === 'employees') {
      this.paginatedEmployees = this.employees.slice(startIndex, endIndex);
    } else if (this.currentView === 'departments') {
      this.paginatedDepartments = this.departments.slice(startIndex, endIndex);
    } else if (this.currentView === 'admins') {
      this.paginatedAdmins = this.admins.slice(startIndex, endIndex);
    }

    this.cdr.detectChanges();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.updatePagination();
  }

  getPagesArray(): number[] {
    return new Array(this.totalPages).fill(0).map((_, i) => i + 1);
  }

  private loadAllEmployees(): void {
    this.isLoading = true;
    const orgId = this.authService.getOrgIdFromToken();

    if (!orgId) {
      this.toastService.show('Organization ID not found', 'error');
      this.stopLoading();
      return;
    }

    this.employeeService
      .getAllEmployees(orgId)
      .pipe(finalize(() => this.stopLoading()))
      .subscribe({
        next: (data: EmployeeResponseDTO[]) => {
          if (this.isOrgAdmin && this.userDepartmentName) {
            this.employees = data.filter((emp) => emp.departmentName === this.userDepartmentName);
            console.log(
              `Filtered employees for department: ${this.userDepartmentName}`,
              this.employees.length
            );
          } else {
            this.employees = data;
          }

          this.stats.totalEmployees = this.employees.length;
          this.updatePagination();
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          this.toastService.show(err.message || 'Error fetching employees', 'error');
        },
      });
  }
  canAccessView(view: string): boolean {
    if (this.isOrganization) return true;
    if (this.isOrgAdmin) {
      return view === 'employees';
    }

    return false;
  }
  private loadAllDepartments(): Promise<void> {
    this.isLoading = true;
    return new Promise((resolve) => {
      this.departmentService
        .getAllByOrg()
        .pipe(
          finalize(() => {
            this.stopLoading();
            resolve();
          })
        )
        .subscribe({
          next: (data) => {
            this.departments = data;
            this.stats.totalDepartments = data.length;

            if (this.departments.length > 0) {
              const countObservables = this.departments.map((dept) =>
                this.employeeService.getEmployeeCountForDepartment(dept.departmentId)
              );

              forkJoin(countObservables).subscribe({
                next: (counts) => {
                  this.departments.forEach((dept, index) => {
                    dept.employeeCount = counts[index];
                  });
                  this.updatePagination();
                  this.cdr.detectChanges();
                },
                error: () => {
                  // Set all counts to 0 on error
                  this.departments.forEach((dept) => (dept.employeeCount = 0));
                  this.updatePagination();
                  this.cdr.detectChanges();
                },
              });
            } else {
              this.updatePagination();
              this.cdr.detectChanges();
            }
          },
          error: (err: HttpErrorResponse) => {
            const errorMsg = err.error?.message || 'Something went wrong!';
            this.toastService.show(errorMsg, 'error');
            this.cdr.detectChanges();
          },
        });
    });
  }

  private loadAllAdmins(): void {
    this.isLoading = true;
    this.orgAdminService
      .getAdminsForCurrentOrg()
      .pipe(finalize(() => this.stopLoading()))
      .subscribe({
        next: (data) => {
          this.admins = data;
          this.stats.totalAdmins = data.length;
          this.updatePagination();
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          const errorMsg = err.error?.message || 'Something went wrong!';
          this.toastService.show(errorMsg, 'error');
          this.cdr.detectChanges();
        },
      });
  }

  // --- Employee CRUD ---
  onAddEmployeeSubmit(): void {
    if (this.addEmployeeForm.invalid) {
      this.toastService.show('Please fill all required fields.', 'error');
      return;
    }

    const formValue = this.addEmployeeForm.value;

    const payload: EmployeeRequestDTO = {
      empName: formValue.empName,
      empEmail: formValue.empEmail,
      phone: formValue.phone,
      bankAccountName: formValue.bankAccountName!,
      bankAccountNo: formValue.bankAccountNo,
      ifscCode: formValue.ifscCode,
      departmentName: formValue.departmentName,
      // ✅ FIX: Send salaryGradeId from form, undefined if null or 0
      salaryGradeId:
        formValue.salaryGradeId && formValue.salaryGradeId > 0
          ? formValue.salaryGradeId
          : undefined,
      balance: formValue.balance || 0,
    };

    this.employeeService.addEmployee(payload).subscribe({
      next: () => {
        this.toastService.show('Employee added successfully!', 'success');
        this.loadAllEmployees();
        this.closeAddSingleEmployeeModal();
      },
      error: (err: HttpErrorResponse) => {
        const errorMsg = err.error?.message || 'Something went wrong!';
        this.toastService.show(errorMsg, 'error');
      },
    });
  }

  openEditEmployeeModal(employee: EmployeeDTO): void {
    if (this.isOrgAdmin && employee.departmentName !== this.userDepartmentName) {
      this.toastService.show('You can only edit employees from your department', 'error');
      return;
    }
    this.editEmployeeForm.patchValue({
      empId: employee.empId,
      empName: employee.empName,
      empEmail: employee.empEmail,
      phone: employee.phone,
      bankAccountName: employee.bankAccountName || '', // ✅ NEW
      bankAccountNo: employee.bankAccountNo,
      ifscCode: employee.ifscCode,
      departmentName: employee.departmentName,
      salaryGradeId: employee.salaryGradeId ?? null, // ✅ Use nullish coalescing
      balance: employee.balance ?? 0, // ✅ Use nullish coalescing
    });
    this.isEditEmployeeModalOpen = true;
  }

  closeEditEmployeeModal(): void {
    this.isEditEmployeeModalOpen = false; // ✅ Correct modal flag
    this.editEmployeeForm.reset(); // ✅ Reset form on close
  }

  onEditEmployeeSubmit(): void {
    if (this.editEmployeeForm.invalid) {
      this.toastService.show('Please fill all required fields.', 'error');
      return;
    }

    const empId = this.editEmployeeForm.value.empId;
    const formValue = this.editEmployeeForm.value;

    const payload: EmployeeRequestDTO = {
      empName: formValue.empName,
      empEmail: formValue.empEmail,
      phone: formValue.phone,
      bankAccountName: formValue.bankAccountName, // ✅ Added
      bankAccountNo: formValue.bankAccountNo,
      ifscCode: formValue.ifscCode,
      departmentName: formValue.departmentName,
      salaryGradeId:
        formValue.salaryGradeId && formValue.salaryGradeId > 0
          ? formValue.salaryGradeId
          : undefined,
      balance: formValue.balance || 0,
    };

    // ✅ FIXED: Added orgId parameter
    this.employeeService.updateEmployee(this.orgId, empId, payload).subscribe({
      next: () => {
        this.toastService.show('Employee details updated successfully!', 'success');
        this.loadAllEmployees();
        this.closeEditEmployeeModal();
      },
      error: (err: HttpErrorResponse) => {
        const errorMsg = err.error?.message || 'Something went wrong!';
        this.toastService.show(errorMsg, 'error');
      },
    });
  }

  deleteEmployee(empId?: number): void {
    if (!empId) return;

    // ✅ Find employee and check permission
    const employee = this.employees.find((e) => e.empId === empId);
    if (this.isOrgAdmin && employee?.departmentName !== this.userDepartmentName) {
      this.toastService.show('You can only delete employees from your department', 'error');
      return;
    }

    if (!confirm('Are you sure you want to delete this employee?')) return;

    this.employeeService.deleteEmployee(this.orgId, empId).subscribe({
      next: () => {
        this.toastService.show('Employee deleted successfully!', 'success');
        this.loadAllEmployees();
      },
      error: (err) => {
        this.toastService.show(err?.message || 'Failed to delete employee', 'error');
      },
    });
  }

  // --- Department CRUD ---
  onDepartmentSubmit(): void {
    if (this.departmentForm.invalid) {
      this.toastService.show('Department name is required.', 'error');
      return;
    }

    const payload = {
      name: this.departmentForm.value.name,
      description: this.departmentForm.value.description,
    };

    const action$ = this.isEditingDepartment
      ? this.departmentService.updateDepartment(this.departmentForm.value.departmentId, payload)
      : this.departmentService.createDepartment(payload);

    action$.subscribe({
      next: () => {
        this.toastService.show(
          `Department ${this.isEditingDepartment ? 'updated' : 'created'}!`,
          'success'
        );
        this.loadAllDepartments();
        this.closeDepartmentModal();
      },
      error: (err) => {
        this.toastService.show(err?.message || 'Failed to save department', 'error');
      },
    });
  }

  deleteDepartment(id: number): void {
    if (!confirm('Are you sure? This might affect assigned admins.')) return;

    this.departmentService.deleteDepartment(id).subscribe({
      next: () => {
        this.toastService.show('Department deleted.', 'success');
        this.loadAllDepartments();
      },
      error: (err: Error) => {
        this.toastService.show(err.message, 'error');
      },
    });
  }

  // --- Admin CRUD ---
  onAdminSubmit(): void {
    if (this.orgAdminForm.invalid) {
      this.toastService.show('Please fill all required fields correctly.', 'error');
      return;
    }

    const requestDTO: OrgAdminRequestDTO = this.orgAdminForm.value;
    const selectedDept = this.departments.find(
      (d) => d.departmentId == this.orgAdminForm.value.departmentName
    );
    requestDTO.departmentName = selectedDept ? selectedDept.name : undefined;

    const action = this.isEditingAdmin
      ? this.orgAdminService.updateOrgAdmin(this.orgAdminForm.value.orgAdminId, requestDTO)
      : this.orgAdminService.createOrgAdmin(requestDTO, this.adminDocumentFile || undefined);

    action.subscribe({
      next: () => {
        this.toastService.show(
          `Admin ${this.isEditingAdmin ? 'updated' : 'created'} successfully!`,
          'success'
        );
        this.loadAllAdmins();
        this.closeOrgAdminModal();
      },
      error: (err: any) => {
        console.error('Error creating/updating admin:', err);
        this.toastService.show(err.message || 'An unexpected error occurred.', 'error');
      },
    });
  }

  deleteAdmin(adminId: number): void {
    if (!confirm('Are you sure you want to delete this admin?')) return;

    this.orgAdminService.deleteOrgAdmin(adminId).subscribe({
      next: () => {
        this.toastService.show('Admin deleted successfully.', 'success');
        this.loadAllAdmins();
      },
      error: (err: any) => {
        console.error('Error deleting admin:', err);
        this.toastService.show(err.message || 'Failed to delete admin.', 'error');
      },
    });
    this.cdr.detectChanges();
  }

  onAdminFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.adminDocumentFile = input?.files?.[0] ?? null;
    this.cdr.detectChanges();
  }

  // --- Batch Upload ---
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.batchFile = input?.files?.[0] ?? null;
    this.cdr.detectChanges();
  }

  onBatchUploadSubmit(): void {
    if (!this.batchFile || this.batchUploadForm.invalid) {
      this.toastService.show('Please select a department and a file to upload.', 'error');
      return;
    }

    const departmentName = this.batchUploadForm.value.departmentName;
    const salaryGradeId = this.batchUploadForm.value.salaryGradeId;
    this.employeeService.addEmployeesBatch(this.batchFile, departmentName).subscribe({
      next: () => {
        this.toastService.show('Batch upload started. Employees will be added shortly.', 'success');
        this.closeBatchUploadModal();
        setTimeout(() => this.loadAllEmployees(), 3000);
      },
      error: (err) => {
        this.toastService.show(err?.message || 'Batch upload failed', 'error');
      },
    });
    this.cdr.detectChanges();
  }

  // --- Profile Management ---
  openProfileModal(): void {
    const orgAdmin = this.admins.find((admin) => admin.email === this.loggedInUserEmail);
    if (orgAdmin) {
      this.profileForm.patchValue({
        name: orgAdmin.name,
        phone: orgAdmin.phone,
      });
    }
    this.isProfileModalOpen = true;
    this.cdr.detectChanges();
  }

  closeProfileModal(): void {
    this.isProfileModalOpen = false;
    this.cdr.detectChanges();
  }

  onProfileUpdateSubmit(): void {
    if (this.profileForm.invalid) return;

    const orgAdmin = this.admins.find((admin) => admin.email === this.loggedInUserEmail);
    if (!orgAdmin) {
      this.toastService.show('Could not find your admin profile to update.', 'error');
      return;
    }

    const payload: OrgAdminRequestDTO = {
      name: this.profileForm.value.name,
      phone: this.profileForm.value.phone,
      email: orgAdmin.email,
    };

    this.orgAdminService.updateOrgAdmin(orgAdmin.orgAdminId, payload).subscribe({
      next: () => {
        this.toastService.show('Profile updated successfully!', 'success');
        this.loadAllAdmins();
        this.loadUserProfile();
        this.closeProfileModal();
      },
      error: (err) => {
        this.toastService.show(err.message, 'error');
      },
    });
    this.cdr.detectChanges();
  }

  // --- Password Management ---
  openChangePasswordModal(): void {
    this.isChangePasswordModalOpen = true;
    this.cdr.detectChanges();
  }

  closeChangePasswordModal(): void {
    this.isChangePasswordModalOpen = false;
    this.changePasswordForm.reset();
    this.cdr.detectChanges();
  }

  onChangePasswordSubmit(): void {
    if (this.changePasswordForm.invalid) {
      this.toastService.show('Please fill all fields correctly.', 'error');
      return;
    }

    this.isLoading = true;
    const { currentPassword, newPassword } = this.changePasswordForm.value;

    this.authService
      .changePassword(currentPassword, newPassword)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (message) => {
          this.toastService.show(message, 'success');
          this.closeChangePasswordModal();
          alert('Password changed successfully! Please log in again.');
          this.authService.logout();
        },
        error: (err) => {
          this.toastService.show(err.message, 'error');
        },
      });
    this.cdr.detectChanges();
  }

  // --- Modal Controls ---
  openAddSingleEmployeeModal(): void {
    if (!this.salaryGrades || this.salaryGrades.length === 0) {
      this.loadAllSalaryGrades();
    }

    // Pre-fill department for org admin
    if (this.isOrgAdmin && this.userDepartmentName) {
      this.addEmployeeForm.patchValue({
        departmentName: this.userDepartmentName,
      });
    } else {
      this.addEmployeeForm.reset();
    }

    this.isAddSingleEmployeeModalOpen = true;
    this.cdr.detectChanges();
  }

  closeAddSingleEmployeeModal(): void {
    this.isAddSingleEmployeeModalOpen = false;
    this.addEmployeeForm.reset({ status: 'Active' });
    this.cdr.detectChanges();
  }

  openBatchUploadModal(): void {
    if (!this.salaryGrades || this.salaryGrades.length === 0) {
      this.loadAllSalaryGrades();
    }

    // Pre-fill department for org admin
    if (this.isOrgAdmin && this.userDepartmentName) {
      this.batchUploadForm.patchValue({
        departmentName: this.userDepartmentName,
      });
    } else {
      this.batchUploadForm.reset();
    }

    this.isBatchUploadModalOpen = true;
    this.cdr.detectChanges();
  }

  closeBatchUploadModal(): void {
    this.isBatchUploadModalOpen = false;
    this.batchFile = null;
    this.batchUploadForm.reset();
    this.cdr.detectChanges();
  }

  openDepartmentModal(dept?: DepartmentResponseDTO): void {
    this.isEditingDepartment = !!dept;
    this.departmentForm.reset(dept || {});
    this.isDepartmentModalOpen = true;
    this.cdr.detectChanges();
  }

  closeDepartmentModal(): void {
    this.isDepartmentModalOpen = false;
    this.cdr.detectChanges();
  }
  openOrgAdminModal(admin?: OrgAdminResponseDTO): void {
    this.isEditingAdmin = !!admin;
    if (admin) {
      const matchingDept = this.departments.find((d) => d.name === admin.departmentName);
      this.orgAdminForm.patchValue({
        orgAdminId: admin.orgAdminId,
        name: admin.name,
        email: admin.email,
        phone: admin.phone,
        departmentName: matchingDept ? matchingDept.departmentId : null,
      });
    } else {
      this.orgAdminForm.reset();
    }
    this.isOrgAdminModalOpen = true;
    this.cdr.detectChanges();
  }

  closeOrgAdminModal(): void {
    this.isOrgAdminModalOpen = false;
    this.adminDocumentFile = null;
    this.orgAdminForm.reset();
    this.cdr.detectChanges();
  }

  // ✅ NEW: Load all salary grades
  loadAllSalaryGrades(): void {
    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) {
      console.error('Organization ID not found');
      return;
    }

    console.log('Loading salary grades for org:', orgId); // ✅ Debug log

    this.salaryGradeService.getAllSalaryGrades(orgId).subscribe({
      next: (grades) => {
        this.salaryGrades = grades;
        console.log('Salary grades loaded:', this.salaryGrades); // ✅ Debug log
        this.cdr.detectChanges(); // ✅ Force change detection
      },
      error: (err) => {
        console.error('Error loading salary grades:', err); // ✅ Debug log
        this.toastService.show(err?.message || 'Failed to load salary grades', 'error');
      },
    });
  }

  // ✅ NEW: Open salary grade modal (Add/Edit)
  openSalaryGradeModal(grade?: SalaryGradeResponseDTO): void {
    if (this.isOrgAdmin) {
      this.toastService.show('You do not have permission to manage salary grades', 'error');
      return;
    }
    this.isEditingSalaryGrade = !!grade;

    if (grade) {
      // Edit mode - pre-populate
      this.salaryGradeForm.patchValue({
        gradeId: grade.gradeId,
        gradeCode: grade.gradeCode,
        basicSalary: grade.basicSalary,
        hra: grade.hra,
        da: grade.da,
        pf: grade.pf,
        allowances: grade.allowances,
      });
    } else {
      // Add mode - reset form
      this.salaryGradeForm.reset({
        gradeId: null,
        basicSalary: 0,
        hra: 0,
        da: 0,
        pf: 0,
        allowances: 0,
      });
    }

    this.isSalaryGradeModalOpen = true;
  }

  // ✅ NEW: Close salary grade modal
  closeSalaryGradeModal(): void {
    this.isSalaryGradeModalOpen = false;
    this.salaryGradeForm.reset();
  }

  // ✅ NEW: Submit salary grade form
  onSalaryGradeSubmit(): void {
    if (this.salaryGradeForm.invalid) {
      this.toastService.show('Please fill all required fields correctly', 'error');
      return;
    }

    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) {
      this.toastService.show('Organization ID not found', 'error');
      return;
    }

    const formValue = this.salaryGradeForm.value;
    const payload: SalaryGradeRequestDTO = {
      gradeCode: formValue.gradeCode!,
      basicSalary: formValue.basicSalary!,
      hra: formValue.hra!,
      da: formValue.da!,
      pf: formValue.pf!,
      allowances: formValue.allowances!,
    };

    if (this.isEditingSalaryGrade && formValue.gradeId) {
      // Update existing grade
      this.salaryGradeService.updateSalaryGrade(orgId, formValue.gradeId, payload).subscribe({
        next: () => {
          this.toastService.show('Salary grade updated successfully!', 'success');
          this.loadAllSalaryGrades();
          this.closeSalaryGradeModal();
        },
        error: (err) => {
          this.toastService.show(err?.message || 'Failed to update salary grade', 'error');
        },
      });
    } else {
      // Create new grade
      this.salaryGradeService.createSalaryGrade(orgId, payload).subscribe({
        next: () => {
          this.toastService.show('Salary grade created successfully!', 'success');
          this.loadAllSalaryGrades();
          this.closeSalaryGradeModal();
        },
        error: (err) => {
          this.toastService.show(err?.message || 'Failed to create salary grade', 'error');
        },
      });
    }
  }

  // ✅ NEW: Delete salary grade
  deleteSalaryGrade(gradeId: number): void {
    if (this.isOrgAdmin) {
      this.toastService.show('You do not have permission to delete salary grades', 'error');
      return;
    }
    if (!confirm('Are you sure you want to delete this salary grade?')) {
      return;
    }

    const orgId = this.authService.getOrgIdFromToken();
    if (!orgId) return;

    this.salaryGradeService.deleteSalaryGrade(orgId, gradeId).subscribe({
      next: () => {
        this.toastService.show('Salary grade deleted successfully!', 'success');
        this.loadAllSalaryGrades();
      },
      error: (err) => {
        this.toastService.show(err?.message || 'Failed to delete salary grade', 'error');
      },
    });
  }

  // ✅ NEW: Calculate total salary
  calculateTotalSalary(grade: SalaryGradeResponseDTO): number {
    return grade.basicSalary + grade.hra + grade.da + grade.allowances - grade.pf;
  }

  // --- Utility Methods ---
  private stopLoading(): void {
    setTimeout(() => {
      this.isLoading = false;
      this.cdr.detectChanges();
    }, 0);
  }
  openAddMoneyModal(): void {
    this.isAddMoneyModalOpen = true;
    this.addMoneyAmount = 10000;
    this.cdr.detectChanges();
  }

  // ✅ NEW: Close Add Money Modal
  closeAddMoneyModal(): void {
    this.isAddMoneyModalOpen = false;
    this.addMoneyAmount = 10000;
    this.cdr.detectChanges();
  }

  // ✅ NEW: Set Quick Amount
  setQuickAmount(amount: number): void {
    this.addMoneyAmount = amount;
    this.cdr.detectChanges();
  }

  // ✅ NEW: Submit Add Money
  onAddMoneySubmit(): void {
    // Validation
    if (this.addMoneyAmount < 10000) {
      this.toastService.show('Minimum recharge amount is ₹10,000', 'error');
      return;
    }

    if (this.addMoneyAmount > 1000000) {
      this.toastService.show('Maximum recharge amount is ₹1,00,00,000', 'error');
      return;
    }

    this.isLoading = true;

    // Call payment service
    this.paymentService.mockPayment(this.orgId, this.addMoneyAmount).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.closeAddMoneyModal();

        this.toastService.show(
          `₹${response.amount.toLocaleString('en-IN')} added successfully!`,
          'success'
        );

        // Refresh balance
        this.loadAccountBalance();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Payment error:', err);

        const errorMessage = err?.error?.error || 'Failed to add money. Please try again.';
        this.toastService.show(errorMessage, 'error');
      },
    });
    this.cdr.detectChanges();
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
        this.authService.logout();
      },
    });
  }
}
