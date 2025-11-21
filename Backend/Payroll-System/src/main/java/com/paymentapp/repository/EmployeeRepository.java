package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Department;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.User;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	Optional<Employee> findByEmpEmail(String email);
    Optional<Employee> findByUser(User user);
    
	List<Employee> findByDepartment(Department department);

    List<Employee> findByOrganizationAndDepartment(Organization organization, Department department);

    List<Employee> findByOrganization(Organization organization);
////changed
//	Collection<EmployeeResponseDTO> findByOrganizationId(Long orgId);

	long countByDepartmentAndDeletedFalse(Department department);
    // ✅ Count active employees in department
  
    
    // ✅ Find active employees in department
    List<Employee> findByDepartmentAndDeletedFalse(Department department);

}
