package com.paymentapp.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
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
@Table(name = "org_admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgAdmin {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orgAdminId;

	private String name;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String phone;

	private String status;
	
	private String bankAccountName;

	private boolean deleted = false;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;
	
	private String bankAccountNo;
	
	private String ifscCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id")
	private Department department;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "user_id", unique = true)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private SalaryGrade salaryGrade;
	
	
	


	
}
