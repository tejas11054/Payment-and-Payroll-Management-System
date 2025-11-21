package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.PaymentRequest;
import com.paymentapp.entity.PaymentRequestApprovalHistory;

public interface PaymentRequestApprovalHistoryRepository extends JpaRepository<PaymentRequestApprovalHistory, Long> {
    List<PaymentRequestApprovalHistory> findByPaymentRequest(PaymentRequest req);
}
