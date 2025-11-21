package com.paymentapp.entity;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_concern")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeConcern {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long concernId;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String attachmentUrl;
	private String status; 

	@CreationTimestamp
	private Instant raisedAt;

	private Long resolvedByUserId;
	private Instant resolvedAt;
	private String responseText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id")
	private Employee employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id")
	private Organization organization;
}