package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
public class OrganizationResponseDTO {

	private Long orgId;
	private String orgName;
	private String email;
	private String phone;
	private String address;
	private String bankAccountNo;
	private String ifscCode;
	private String bankName;
	private int employeeCount;
	private BigDecimal accountBalance;
	private String status;
	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;
	  private List<VerificationDocumentDTO> verificationDocuments;
}
