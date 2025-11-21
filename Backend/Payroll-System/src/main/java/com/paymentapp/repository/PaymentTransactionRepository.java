package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Organization;
import com.paymentapp.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrganization(Organization org);
}