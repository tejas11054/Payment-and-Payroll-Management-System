package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private Long paymentId;
    private BigDecimal amount;
    private String invoiceReference;
    private String status;
    
    private Long orgId;
    private String orgName; 
    
    private Long vendorId;
    private String vendorName; 
    private String bankAccountNo; 
    private String ifscCode; 
    private String bankName; 
    
    private Long requestedById;
    private Long approvedById;
    
    private Instant createdAt;
    private Instant processedAt;
}
