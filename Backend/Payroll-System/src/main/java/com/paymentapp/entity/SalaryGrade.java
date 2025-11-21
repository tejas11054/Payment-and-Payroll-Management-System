package com.paymentapp.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
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
@Table(name = "salary_grade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryGrade {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long gradeId;
	
	@Column(unique = true)
	private String gradeCode; 

	private BigDecimal basicSalary;
	private BigDecimal hra;
	private BigDecimal da;
	private BigDecimal pf;
	private BigDecimal allowances;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;
	
	private boolean deleted = false;

	
}
