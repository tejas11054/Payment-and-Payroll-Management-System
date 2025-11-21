package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorProfileDTO {
    private Long vendorId;
    private String name;
    private String contactEmail;
    private String phone;
    private String vendorType;
    private BigDecimal balance;
    
    private String bankName;
    private String bankAccountNo;
    private String ifscCode;
    private String accountHolderName;
    
    private String address;
    private String city;
    private String state;
    private String pincode;
    
    private Long orgId;
    private String orgName;
    
    private Instant createdAt;
}
