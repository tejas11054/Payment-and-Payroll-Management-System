package com.paymentapp.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salary_slip")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalarySlip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slipId;

    @Column(nullable = false)
    private String period;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant generatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id")
    private Employee employee;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_admin_id")
    private OrgAdmin orgAdmin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private SalaryDisbursalLine disbursalLine;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursal_id")
    private SalaryDisbursalRequest disbursal;
    
    public SalaryDisbursalRequest getDisbursalRequest() {
        if (disbursalLine != null) {
            return disbursalLine.getDisbursalRequest();
        }
        return disbursal;
    }
}
