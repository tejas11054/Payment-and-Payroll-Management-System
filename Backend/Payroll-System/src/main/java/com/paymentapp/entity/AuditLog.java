package com.paymentapp.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false)
    private String performedByEmail;

    @Column(nullable = false)
    private Long performedByUserId;

    @Column(nullable = false)
    private String performedByRole;

    @Column(nullable = false)
    private String actionPerformed;

    @Column(nullable = false)
    private String targetResourceType;

    @Column(nullable = false)
    private Long targetResourceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant actionTimestamp;
}
