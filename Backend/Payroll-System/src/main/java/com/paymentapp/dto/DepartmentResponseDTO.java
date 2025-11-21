package com.paymentapp.dto;

import lombok.Data;

@Data
public class DepartmentResponseDTO {
	private Long departmentId;
	private String name;
	private String description;
	private Long organizationId;
	private Long employeeCount;

	private Long adminCount;
}
