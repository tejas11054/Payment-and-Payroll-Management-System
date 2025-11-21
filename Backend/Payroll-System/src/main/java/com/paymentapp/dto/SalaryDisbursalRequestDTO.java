package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class SalaryDisbursalRequestDTO {
	  private Long disbursalId;
	    private Long orgId;
	    private String orgName;
	    private String period;
	    private String status;
	    private BigDecimal totalAmount;
	    private String remarks;
	    private Instant createdAt;
	    private List<SalaryDisbursalLineDTO> lines;
}