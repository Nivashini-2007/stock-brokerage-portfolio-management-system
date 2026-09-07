package com.stockbroker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Lightweight audit trail (SRS FR15/FR3 "log every data access/change
 * event"). Recorded explicitly from sensitive mutations (login, order
 * placement, fund transfer, KYC review, role/status change) rather than via
 * a blanket AOP interceptor on every read - documented as a partial
 * implementation of the full audit requirement.
 */
@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(nullable = false)
    private String action;

    private String entityType;

    private String entityId;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
