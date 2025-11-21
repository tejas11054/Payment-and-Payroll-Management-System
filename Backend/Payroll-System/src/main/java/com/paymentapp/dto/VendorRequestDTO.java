package com.paymentapp.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class VendorRequestDTO {
    private String name;
    private String vendorType;
    private String bankName;
    private String bankAccountNo;
    private String ifscCode;
    private String contactEmail;
    private String phone;
    private String fileUrl; 
    private BigDecimal balance = BigDecimal.ZERO;
    
}

