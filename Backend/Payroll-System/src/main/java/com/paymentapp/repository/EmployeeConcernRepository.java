package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paymentapp.entity.EmployeeConcern;

@Repository
public interface EmployeeConcernRepository extends JpaRepository<EmployeeConcern, Long> {
	@Query("SELECT COUNT(c) FROM EmployeeConcern c WHERE c.employee.id = :employeeId AND c.status = :status")
	long countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") String status);

	List<EmployeeConcern> findByEmployee_EmpIdOrderByRaisedAtDesc(Long employeeId);

}
