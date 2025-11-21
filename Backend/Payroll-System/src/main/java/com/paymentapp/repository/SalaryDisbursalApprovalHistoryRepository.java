package com.paymentapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.paymentapp.entity.SalaryDisbursalApprovalHistory;

public interface SalaryDisbursalApprovalHistoryRepository extends JpaRepository<SalaryDisbursalApprovalHistory, Long> {
}
