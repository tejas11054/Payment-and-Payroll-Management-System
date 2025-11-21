package com.paymentapp.entity;

import java.math.BigDecimal;

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
@Table(name = "salary_disbursal_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDisbursalLine {
	
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineId;
    
    private BigDecimal grossSalary;
    private BigDecimal deductions;
    private BigDecimal netAmount;

    private String status = "PENDING"; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursal_id")
    private SalaryDisbursalRequest disbursalRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id")
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_admin_id")
    private OrgAdmin orgAdmin;


}
