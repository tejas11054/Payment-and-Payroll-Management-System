package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Department;
import com.paymentapp.entity.OrgAdmin;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.User;

public interface OrgAdminRepository extends JpaRepository<OrgAdmin, Long> {

	 Optional<OrgAdmin> findByEmail(String email);
	Optional<OrgAdmin> findByOrgAdminIdAndDeletedFalse(Long orgAdminId);

	List<OrgAdmin> findByOrganizationAndDeletedFalse(Organization organization);

	long countByDepartment(Department department);

	long countByDepartmentAndDeletedFalse(Department department);

	boolean existsByEmailIgnoreCase(String email);

	List<OrgAdmin> findByDepartment(Department department);

	List<OrgAdmin> findByOrganization(Organization organization);

	List<OrgAdmin> findByOrganizationAndDepartment(Organization org, Department dept);

	Optional<OrgAdmin> findByUser(User user);

	Optional<Department> findByNameAndOrganization(String name, Organization organization);

	Optional<Department> findByNameIgnoreCaseAndOrganization(String name, Organization organization);

	Optional<Department> findByNameAndOrganizationAndDeletedFalse(String name, Organization organization);
	Optional<OrgAdmin> findByEmailAndOrganizationAndDeletedFalse(String email,
			Organization organization);

	List<OrgAdmin> findByOrganization_OrgId(Long orgId);


}
