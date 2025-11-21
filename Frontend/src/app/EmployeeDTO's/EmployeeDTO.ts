/**
 * This DTO represents the data for a single employee
 * as expected by the frontend dashboard and forms.
 */
// This DTO is for sending data TO the backend when creating/updating an employee
export interface EmployeeRequestDTO {
  empName: string;
  empEmail: string;
  phone: string;
  bankAccountName?: string; // ✅ NEW
  bankAccountNo: string;
  ifscCode: string;
  departmentName: string;
  salaryGradeId?: number; // ✅ Use gradeId instead of gradeCode
  balance?: number;

  // documentUrl?: string;    // Optional
}
// private Long empId;
// private String empName;
// private String empEmail;
// private String phone;
// private String bankAccountNo;
// private String ifscCode;
// private String status;
// private Long organizationId;
// private String departmentName;
// private Long salaryGradeId;
export interface EmployeeResponseDTO {
  empId: number;
  empName: string;
  empEmail: string;
  phone: string;
   bankAccountName?: string;  // ✅ NEW
  bankAccountNo: string;
  ifscCode: string;
  status: string;
  organizationId: number;
  departmentName: string;
  salaryGradeId?: number;
  gradeCode?: string; // ✅ ADD THIS
  balance?: number; // ✅ Optional
}
// This DTO is for receiving data FROM the backend
export interface EmployeeDTO {
  empId?: number; // Optional because it won't exist on creation
  empName: string;
  empEmail: string;
  phone: string;
   bankAccountName?: string;  // ✅ NEW
  bankAccountNo: string;
  ifscCode: string;
  status: string;
  organizationId?: number;
  departmentName?: string;
  salaryGradeId?: number; // ✅ ADD THIS
  gradeCode?: string; // ✅ ADD THIS
  balance?: number; // ✅ ADD THIS (if you're using it)
}

// You can keep other dashboard-related DTOs in this file as well
// For example:
export interface DashboardStatsDTO {
  totalEmployees: number;
  pendingConcerns: number;
  lastPayrollAmount: number;
}

export interface PayrollRequestDTO {
  period: string; // e.g., "October 2025"
}

export interface ContactMessage {
  id: number;
  name: string;
  email: string;
  subject: string;
  message: string;
  status: 'UNREAD' | 'READ' | 'REPLIED';
  createdAt: string; // ISO date string
}
