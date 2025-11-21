package com.paymentapp.entity;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;

@Entity
@Table(name = "payment_receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptId;

    private BigDecimal amount;
    private String bankReference;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentRequest paymentRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @CreationTimestamp
    private Instant createdAt;
}
