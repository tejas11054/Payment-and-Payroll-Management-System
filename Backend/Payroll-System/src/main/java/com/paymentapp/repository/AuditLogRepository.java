package com.paymentapp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paymentapp.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by user ID
     */
    List<AuditLog> findByPerformedByUserId(Long userId);
    
    /**
     * Find audit logs by role
     */
    List<AuditLog> findByPerformedByRole(String role);
    
    /**
     * Find audit logs by action performed
     */
    List<AuditLog> findByActionPerformed(String action);
    
    /**
     * Find audit logs by target resource type
     */
    List<AuditLog> findByTargetResourceType(String resourceType);
    
    /**
     * Find audit logs by resource ID
     */
    List<AuditLog> findByTargetResourceId(Long resourceId);
    
    /**
     * Find audit logs by email
     */
    List<AuditLog> findByPerformedByEmail(String email);
}
