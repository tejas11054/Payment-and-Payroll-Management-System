package com.paymentapp.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.entity.AuditLog;
import com.paymentapp.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        System.out.println("\n ════════════════════════════════════════");
        System.out.println(" GET ALL AUDIT LOGS");
        System.out.println(" ════════════════════════════════════════");
        
        try {
            List<AuditLog> logs = auditLogRepository.findAll(
                Sort.by(Sort.Direction.DESC, "actionTimestamp")
            );
            
            System.out.println(" Found " + logs.size() + " audit log(s)");
            System.out.println(" ════════════════════════════════════════\n");
            
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            System.err.println(" ERROR fetching audit logs:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println(" ════════════════════════════════════════\n");
            
            throw new RuntimeException("Failed to fetch audit logs: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable Long userId) {
        System.out.println("\n Getting audit logs for user ID: " + userId);
        
        List<AuditLog> logs = auditLogRepository.findByPerformedByUserId(userId);
        
        System.out.println(" Found " + logs.size() + " log(s) for user\n");
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByRole(@PathVariable String role) {
        System.out.println("\n Getting audit logs for role: " + role);
        
        List<AuditLog> logs = auditLogRepository.findByPerformedByRole(role);
        
        System.out.println(" Found " + logs.size() + " log(s) for role\n");
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(@PathVariable String action) {
        System.out.println("\n Getting audit logs for action: " + action);
        
        List<AuditLog> logs = auditLogRepository.findByActionPerformed(action);
        
        System.out.println(" Found " + logs.size() + " log(s) for action\n");
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/resource/{resourceType}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByResourceType(@PathVariable String resourceType) {
        System.out.println("\n Getting audit logs for resource type: " + resourceType);
        
        List<AuditLog> logs = auditLogRepository.findByTargetResourceType(resourceType);
        
        System.out.println(" Found " + logs.size() + " log(s) for resource type\n");
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getAuditLogsCount() {
        long count = auditLogRepository.count();
        System.out.println("\n Total audit logs count: " + count + "\n");
        return ResponseEntity.ok(count);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<AuditLog>> getFilteredAuditLogs(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long userId) {
        
        System.out.println("\n Filtering audit logs:");
        System.out.println("   Role: " + role);
        System.out.println("   Action: " + action);
        System.out.println("   Resource Type: " + resourceType);
        System.out.println("   User ID: " + userId);

        List<AuditLog> logs = auditLogRepository.findAll(
            Sort.by(Sort.Direction.DESC, "actionTimestamp")
        );

        if (role != null && !role.isEmpty()) {
            logs = logs.stream()
                .filter(log -> log.getPerformedByRole().equals(role))
                .toList();
        }

        if (action != null && !action.isEmpty()) {
            logs = logs.stream()
                .filter(log -> log.getActionPerformed().equals(action))
                .toList();
        }

        if (resourceType != null && !resourceType.isEmpty()) {
            logs = logs.stream()
                .filter(log -> log.getTargetResourceType().equals(resourceType))
                .toList();
        }

        if (userId != null) {
            logs = logs.stream()
                .filter(log -> log.getPerformedByUserId().equals(userId))
                .toList();
        }

        System.out.println(" Found " + logs.size() + " filtered log(s)\n");
        return ResponseEntity.ok(logs);
    }
}
