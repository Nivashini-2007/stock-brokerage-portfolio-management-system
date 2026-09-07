package com.stockbroker.backend.entity;

import com.stockbroker.backend.enums.NotificationChannel;
import com.stockbroker.backend.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Mapped to "is_read" - "read" is a reserved word in MySQL and fails
    // unquoted DDL generation.
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
