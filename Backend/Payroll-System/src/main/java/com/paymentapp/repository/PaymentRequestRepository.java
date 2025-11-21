package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Organization;
import com.paymentapp.entity.PaymentRequest;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    List<PaymentRequest> findByOrganizationOrgId(Long orgId);

	List<PaymentRequest> findByStatus(String string);
	List<PaymentRequest> findByOrganization(Organization organization);
}
