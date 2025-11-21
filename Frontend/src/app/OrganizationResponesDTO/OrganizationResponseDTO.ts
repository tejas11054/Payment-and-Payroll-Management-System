export interface OrganizationResponseDTO {
  orgId: number;
  orgName: string;
  email: string;
  phone: string;
  address: string;
  status: string;
  createdAt: string; 
  verificationDocs?: { docType: string; cloudUrl: string }[];
}

export interface AuditLogDTO {
  logId: number;
  performedByEmail: string;
  performedByRole: string;
  actionPerformed: string;
  targetResourceType: string;
  targetResourceId: number;
  actionTimestamp: string; 
}
