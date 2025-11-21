package com.paymentapp.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salary_disbursal_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDisbursalRequest {
	
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long disbursalId;
   
    private String period; 

    private BigDecimal totalAmount;

    private String status = "PENDING"; 

    private String remarks; 
    
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant processedAt;

    @OneToMany(mappedBy = "disbursalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalaryDisbursalLine> lines = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;
}