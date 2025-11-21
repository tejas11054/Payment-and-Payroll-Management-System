package com.paymentapp.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrgAdminRequestDTO {
	private String name;
	private String email;
	private String phone;
	private String departmentName;
	private Long salaryGradeId;
	private String fileUrl;
	private String bankAccountName;
	private String bankAccountNo;

	private String ifscCode;
	private String password; 
}
