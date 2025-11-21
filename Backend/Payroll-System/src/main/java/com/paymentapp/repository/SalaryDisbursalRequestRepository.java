package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paymentapp.entity.SalaryDisbursalRequest;

public interface SalaryDisbursalRequestRepository extends JpaRepository<SalaryDisbursalRequest, Long> {
	List<SalaryDisbursalRequest> findByStatus(String status);
	// Check if pending/approved request exists for this period
	@Query("SELECT COUNT(s) > 0 FROM SalaryDisbursalRequest s " +
	       "WHERE s.organization.orgId = :orgId " +
	       "AND s.period = :period " +
	       "AND s.status IN ('PENDING', 'APPROVED')")
	boolean existsByOrgAndPeriodAndStatus(@Param("orgId") Long orgId, 
	                                      @Param("period") String period);

	// Alternative: Get existing request for reference
	List<SalaryDisbursalRequest> findByOrganization_OrgIdAndPeriodAndStatusIn(
	    Long orgId, 
	    String period, 
	    List<String> statuses
	);
}
