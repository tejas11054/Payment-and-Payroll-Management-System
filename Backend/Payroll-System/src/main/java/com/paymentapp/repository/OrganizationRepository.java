// OrganizationRepository.java

package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.paymentapp.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    Optional<Organization> findByOrgIdAndDeletedFalse(Long orgId);
    
    Optional<Organization> findByEmail(String email);
    
    List<Organization> findByStatus(String status);
    
    boolean existsByOrgName(String orgName);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    boolean existsByBankAccountNo(String bankAccountNo);
    
    boolean existsByOrgNameAndOrgIdNot(String orgName, Long orgId);
    
    boolean existsByEmailAndOrgIdNot(String email, Long orgId);
    
    boolean existsByPhoneAndOrgIdNot(String phone, Long orgId);
    
    boolean existsByBankAccountNoAndOrgIdNot(String bankAccountNo, Long orgId);
}
