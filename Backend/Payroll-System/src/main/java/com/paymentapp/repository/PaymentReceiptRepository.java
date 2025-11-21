package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.PaymentReceipt;
import com.paymentapp.entity.PaymentRequest;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {
	Optional<PaymentReceipt> findByPaymentRequest(PaymentRequest req);

	List<PaymentReceipt> findByOrganizationOrgId(Long orgId);

	List<PaymentReceipt> findByVendorVendorId(Long vendorId);
}
