package com.stockbroker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * JWT ids (jti) invalidated by logout, password change, or a security
 * breach event (SRS Appendix D "Token Blacklisting").
 */
@Entity
@Table(name = "blacklisted_tokens")
@Data
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
