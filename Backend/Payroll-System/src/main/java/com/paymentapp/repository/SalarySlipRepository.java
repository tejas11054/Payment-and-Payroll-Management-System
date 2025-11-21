package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.paymentapp.entity.SalarySlip;

public interface SalarySlipRepository extends JpaRepository<SalarySlip, Long> {

	List<SalarySlip> findByEmployee_EmpIdOrderByGeneratedAtDesc(Long empId);
}
