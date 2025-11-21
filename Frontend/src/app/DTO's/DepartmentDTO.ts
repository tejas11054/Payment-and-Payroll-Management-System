export interface DepartmentRequestDTO {
    name: string;
    description: string;
}

export interface DepartmentResponseDTO {
    departmentId: number;
    name: string;
    description: string;
    organizationId: number;
    employeeCount: number;
}

export interface OrgAdminRequestDTO {
    name: string;
    email: string;
    phone: string;
    departmentName?: string; // Optional because a top-level admin might not have one
    fileUrl?: string;
    password?:string;
}

export interface OrgAdminResponseDTO {
    orgAdminId: number;
    name: string;
    email: string;
    phone: string;
    status: string;
    organizationId: number;
    departmentName: string;

}
