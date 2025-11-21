package com.paymentapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.paymentapp.entity.SalaryDisbursalLine;

public interface SalaryDisbursalLineRepository extends JpaRepository<SalaryDisbursalLine, Long> {
}
