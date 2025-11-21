package com.paymentapp.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salary_disbursal_approval_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDisbursalApprovalHistory {
	
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    private String action = "REQUESTED"; 
    private String comment;
    
    @CreationTimestamp
    private Instant actedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acted_by")
    private User actedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursal_id", nullable = false)
    private SalaryDisbursalRequest disbursal;

}