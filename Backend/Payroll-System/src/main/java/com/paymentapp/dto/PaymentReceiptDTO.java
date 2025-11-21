package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceiptDTO {
    private Long receiptId;
    private Long paymentId;
    private BigDecimal amount;
    private String bankReference;
    private String status;
    private Long vendorId;
    private String vendorName;
    private Long orgId;
    private String orgName;
    private Instant createdAt;
}