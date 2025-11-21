package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Data;

@Data
public class VendorResponseDTO {
    private Long vendorId;
    private String name;
    private String vendorType;
    private String bankName;
    private String bankAccountNo;
    private String ifscCode;
    private String contactEmail;
    private String phone;
    private boolean deleted;
    private Instant createdAt;
    private Long organizationId;
    private BigDecimal balance;
}
