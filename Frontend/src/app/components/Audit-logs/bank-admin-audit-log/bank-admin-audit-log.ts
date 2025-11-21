import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditLog, BankadminService } from '../../../services/auditlog/bankAdmin/bankadmin-service';
import { ToastService } from '../../../services/notification/toast-service';
import { DarkModeService } from '../../../services/darkMode/dark-mode';


@Component({
  selector: 'app-bank-admin-audit-log',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bank-admin-audit-log.html',
  styleUrls: ['./bank-admin-audit-log.css'],
})
export class BankAdminAuditLog implements OnInit {
  // ================= Data =================
  auditLogs: AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  
  // ================= UI State =================
  isLoading: boolean = true;
  isDarkMode: boolean = false;
  totalLogs: number = 0;
  
  // ================= Filters =================
  selectedRole: string = 'all';
  selectedAction: string = 'all';
  selectedResourceType: string = 'all';
  searchQuery: string = '';
  
  // ================= Filter Options =================
  roles = ['all', 'BANK_ADMIN', 'ROLE_ORGANIZATION', 'ROLE_EMPLOYEE','VENDOR','ROLE_BANK_ADMIN'];
  actions = [
    'all',
    'CREATE',
    'UPDATE',
    'DELETE',
    'APPROVE',
    'REJECT',
    'LOGIN',
    'LOGOUT',
    'REGISTER',
    'PAYMENT_APPROVED',
    'PAYMENT_REJECTED',
    'SALARY_APPROVED',
    'SALARY_REJECTED',
  ];
  resourceTypes = [
    'all',
    'ORGANIZATION',
    'EMPLOYEE',
    'SALARY_REQUEST',
    'PAYMENT_REQUEST',
    'VENDOR',
    'DEPARTMENT',
    'GRADE',
  ];
  
  // ================= Pagination =================
  currentPage: number = 1;
  itemsPerPage: number = 20;
  totalPages: number = 1;
  
  Math = Math;
  
  // ================= Services =================
  auditLogService = inject(BankadminService);
  toastService = inject(ToastService);
  darkModeService = inject(DarkModeService);
  cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.darkModeService.isDarkMode$.subscribe((isDark: boolean) => {
      this.isDarkMode = isDark;
      this.cdr.markForCheck();
    });

    this.loadAuditLogs();
  }

  // ================= Load Data =================
  loadAuditLogs(): void {
    this.isLoading = true;
    
    this.auditLogService.getAllAuditLogs().subscribe({
      next: (logs) => {
        this.auditLogs = logs.sort((a, b) => 
          new Date(b.actionTimestamp).getTime() - new Date(a.actionTimestamp).getTime()
        );
        this.totalLogs = logs.length;
        this.applyFilters();
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('Error loading audit logs:', error);
        this.toastService.show('Failed to load audit logs', 'error');
        this.isLoading = false;
        this.auditLogs = [];
        this.filteredLogs = [];
        this.cdr.markForCheck();
      },
    });
  }

  // ================= Apply Filters =================
  applyFilters(): void {
    let filtered = [...this.auditLogs];

    if (this.selectedRole !== 'all') {
      filtered = filtered.filter(log => log.performedByRole === this.selectedRole);
    }

    if (this.selectedAction !== 'all') {
      filtered = filtered.filter(log => log.actionPerformed === this.selectedAction);
    }

    if (this.selectedResourceType !== 'all') {
      filtered = filtered.filter(log => log.targetResourceType === this.selectedResourceType);
    }

    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(log =>
        log.performedByEmail.toLowerCase().includes(query) ||
        log.actionPerformed.toLowerCase().includes(query) ||
        log.targetResourceType.toLowerCase().includes(query) ||
        log.performedByRole.toLowerCase().includes(query)
      );
    }

    this.filteredLogs = filtered;
    this.totalPages = Math.ceil(this.filteredLogs.length / this.itemsPerPage);
    this.currentPage = 1;
    this.cdr.markForCheck();
  }

  // ================= Pagination =================
  getPaginatedLogs(): AuditLog[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.filteredLogs.slice(startIndex, endIndex);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.cdr.markForCheck();
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.cdr.markForCheck();
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.cdr.markForCheck();
    }
  }

  // ================= Helpers =================
  getActionBadgeClass(action: string): string {
    const actionMap: { [key: string]: string } = {
      'CREATE': 'badge-success',
      'UPDATE': 'badge-info',
      'DELETE': 'badge-danger',
      'APPROVE': 'badge-success',
      'REJECT': 'badge-danger',
      'LOGIN': 'badge-primary',
      'LOGOUT': 'badge-secondary',
      'REGISTER': 'badge-success',
      'PAYMENT_APPROVED': 'badge-success',
      'PAYMENT_REJECTED': 'badge-danger',
      'SALARY_APPROVED': 'badge-success',
      'SALARY_REJECTED': 'badge-danger',
    };
    return actionMap[action] || 'badge-default';
  }

  getRoleBadgeClass(role: string): string {
    const roleMap: { [key: string]: string } = {
      'BANK_ADMIN': 'role-badge-bank',
      'ROLE_ORGANIZATION': 'role-badge-org',
      'ROLE_EMPLOYEE': 'role-badge-employee',
    };
    return roleMap[role] || 'role-badge-default';
  }

  formatTimestamp(timestamp: string): string {
    return new Date(timestamp).toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  }

  // ================= Export =================
  exportToCSV(): void {
    const headers = [
      'Log ID',
      'User Email',
      'User ID',
      'Role',
      'Action',
      'Resource Type',
      'Resource ID',
      'Timestamp',
    ];

    const csvData = this.filteredLogs.map(log => [
      log.logId,
      log.performedByEmail,
      log.performedByUserId,
      log.performedByRole,
      log.actionPerformed,
      log.targetResourceType,
      log.targetResourceId,
      this.formatTimestamp(log.actionTimestamp),
    ]);

    const csv = [
      headers.join(','),
      ...csvData.map(row => row.join(',')),
    ].join('\n');

    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-logs-${new Date().toISOString()}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);

    this.toastService.show('Audit logs exported successfully!', 'success');
  }

  // ================= Clear Filters =================
  clearFilters(): void {
    this.selectedRole = 'all';
    this.selectedAction = 'all';
    this.selectedResourceType = 'all';
    this.searchQuery = '';
    this.applyFilters();
  }
}
