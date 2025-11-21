package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Document;
import com.paymentapp.entity.OrganizationVerificationDocument;

public interface DocumentRepository extends JpaRepository<Document, Long> {
	List<Document> findByOwnerIdAndOwnerType(Long ownerId, String ownerType);
	  List<OrganizationVerificationDocument> findByOrganization_OrgId(Long orgId);

}

