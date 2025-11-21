package com.paymentapp.dto;

import lombok.Data;

@Data
public class OrgAdminResponseDTO {
	private Long orgAdminId;
	private String name;
	private String email;
	private String phone;
	private String status;
	private Long organizationId;
	private String departmentName;
	private Long salaryGradeId;
	private String bankAccountName;
	private String bankAccountNo;
    private String gradeCode;  
	private String ifscCode;
    private Long departmentId;          
    private SalaryGradeResponseDTO salaryGrade;
}
