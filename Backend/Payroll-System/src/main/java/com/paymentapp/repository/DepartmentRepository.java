package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Department;
import com.paymentapp.entity.Organization;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	// ✅ Active departments only
	List<Department> findByOrganizationAndDeletedFalse(Organization organization);

	// ✅ Find by name (exact match)
	Optional<Department> findByNameAndOrganizationAndDeletedFalse(String name, Organization organization);

	// ✅ Find by name (case-insensitive)
	Optional<Department> findByNameIgnoreCaseAndOrganizationAndDeletedFalse(String name, Organization organization);

	// ✅ Find first (handles duplicates)
	Optional<Department> findFirstByNameAndOrganizationAndDeletedFalse(String name, Organization organization);

	// ✅ Count active departments
	long countByOrganizationAndDeletedFalse(Organization organization);

	// ✅ Check if exists
	boolean existsByNameAndOrganizationAndDeletedFalse(String name, Organization organization);

	Optional<Department> findByNameIgnoreCaseAndOrganization(String trim, Organization organization);

	Optional<Department> findByNameIgnoreCaseAndOrganizationAndDeletedTrue(String deptName, Organization organization);
}
