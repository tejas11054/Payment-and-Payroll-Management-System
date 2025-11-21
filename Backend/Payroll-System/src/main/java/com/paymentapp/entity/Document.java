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
@Table(name = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long documentId;

	@Column(name = "document_owner_type", nullable = false, length = 50)
	private String ownerType;

	@Column(name = "document_owner_id", nullable = false)
	private Long ownerId;

	@Column(name = "cloud_url", nullable = false, length = 512)
	private String cloudUrl;

	@Column(name = "doc_type", nullable = false, length = 100)
	private String docType;

	@Column(name = "uploaded_by_user_id", nullable = false)
	private Long uploadedBy;

	@CreationTimestamp
	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", nullable = false)
	private Organization organization;
}
