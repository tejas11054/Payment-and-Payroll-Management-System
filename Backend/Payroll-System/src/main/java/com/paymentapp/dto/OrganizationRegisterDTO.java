package com.paymentapp.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class OrganizationRegisterDTO {

	    private String orgName;
	    private String email;
	    private String phone;
	    private String address;
	    private String bankAccountNo;
	    private String ifscCode;
	    private String bankName;
	    private int employeeCount = 0;
	    private BigDecimal accountBalance = BigDecimal.ZERO;
	    private List<String> verificationDocs;

}
