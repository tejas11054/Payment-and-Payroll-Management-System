package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Organization;
import com.paymentapp.entity.SalaryGrade;

public interface SalaryGradeRepository extends JpaRepository<SalaryGrade, Long> {
    List<SalaryGrade> findByOrganization(Organization organization);

    Optional<SalaryGrade> findByOrganizationOrgIdAndGradeCode(Long orgId, String gradeCode);
    
    Optional<SalaryGrade> findByGradeIdAndOrganization(Long gradeId, Organization organization);


}
