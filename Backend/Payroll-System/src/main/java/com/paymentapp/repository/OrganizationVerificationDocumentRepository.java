package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paymentapp.entity.OrganizationVerificationDocument;

@Repository
public interface OrganizationVerificationDocumentRepository extends JpaRepository<OrganizationVerificationDocument, Long> {
	
	List<OrganizationVerificationDocument> findByOrganization_OrgId(Long orgId);
}
