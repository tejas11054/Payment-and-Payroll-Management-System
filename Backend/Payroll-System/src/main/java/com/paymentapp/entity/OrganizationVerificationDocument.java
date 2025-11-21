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
@Table(name = "organization_verification_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationVerificationDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long docId;

	private String filename;
	private String cloudUrl;
	private String docType; 
	private Long uploadedByUserId; 

	private String status; 

	@CreationTimestamp
	private Instant uploadedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;
}
