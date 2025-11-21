package com.paymentapp.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EmployeeRequestDTO {
	private String empName;
	private String empEmail;
	private String phone;
	private String bankAccountNo;
	private String ifscCode;
	private String departmentName;
	  private String bankAccountName;
	private Long salaryGradeId;
	private String documentUrl;
}
