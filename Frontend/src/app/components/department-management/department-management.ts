import { Component, inject, OnInit } from '@angular/core';
import { OrgAuthservice } from '../../services/orgDashboard/org-authservice';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastService } from '../../services/notification/toast-service';
import { LoginAuthentication } from '../../services/login/login-authentication';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-department-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './department-management.html',
  styleUrl: './department-management.css'
})
export class DepartmentManagement implements OnInit {
  private orgService = inject(OrgAuthservice);
  private authService = inject(LoginAuthentication);  // ✅ ADDED
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);

  // ✅ ADDED: Store orgId
  orgId: number = 0;

  deptForm: FormGroup;
  departments: any[] = [];

  constructor() {
    this.deptForm = this.fb.group({
      name: ['', Validators.required],        // ✅ Changed from deptName to name
      description: ['']                        // ✅ ADDED description field
    });
  }

  ngOnInit(): void {
    // ✅ Get orgId from token
    const orgId = this.authService.getOrgIdFromToken();
    if (orgId) {
      this.orgId = orgId;
      this.loadDepartments();
    } else {
      this.toast.show('Organization ID not found', 'error');
    }
  }

  // ✅ UPDATED: Pass orgId parameter
  loadDepartments(): void {
    this.orgService.getAllDepartments(this.orgId).subscribe({
      next: (res) => {
        this.departments = res;
      },
      error: (err) => {
        console.error('Failed to load departments:', err);
        this.toast.show('Failed to load departments', 'error');
      }
    });
  }

  // ✅ UPDATED: Pass orgId parameter
  onAddDept(): void {
    if (this.deptForm.invalid) {
      this.toast.show('Please enter department name', 'error');
      return;
    }

    const payload = {
      name: this.deptForm.value.name,
      description: this.deptForm.value.description || ''
    };

    this.orgService.addDepartment(this.orgId, payload).subscribe({
      next: (dept) => {
        this.departments.push(dept);
        this.toast.show('Department added successfully!', 'success');
        this.deptForm.reset();
        this.loadDepartments(); // ✅ Reload to get fresh data
      },
      error: (err) => {
        console.error('Failed to add department:', err);
        this.toast.show(err?.message || 'Failed to add department', 'error');
      }
    });
  }

  // ✅ UPDATED: Use correct property names
  deleteDept(id: number): void {
    if (!confirm('Are you sure you want to delete this department?')) return;

    this.orgService.deleteDepartment(id).subscribe({
      next: () => {
        // ✅ Use departmentId instead of deptId
        this.departments = this.departments.filter(d => d.departmentId !== id);
        this.toast.show('Department deleted successfully', 'success');
      },
      error: (err) => {
        console.error('Failed to delete department:', err);
        this.toast.show(err?.message || 'Failed to delete department', 'error');
      }
    });
  }

  // ✅ UPDATED: Use correct property names and better UX
  editDept(dept: any): void {
    const newName = prompt('Edit department name:', dept.name);
    if (!newName || newName.trim() === '') return;

    const payload = {
      name: newName.trim(),
      description: dept.description || ''
    };

    this.orgService.updateDepartment(dept.departmentId, payload).subscribe({
      next: (updatedDept) => {
        // ✅ Update the department in the list
        const index = this.departments.findIndex(d => d.departmentId === dept.departmentId);
        if (index !== -1) {
          this.departments[index] = updatedDept;
        }
        this.toast.show('Department updated successfully', 'success');
      },
      error: (err) => {
        console.error('Failed to update department:', err);
        this.toast.show(err?.message || 'Update failed', 'error');
      }
    });
  }
}
