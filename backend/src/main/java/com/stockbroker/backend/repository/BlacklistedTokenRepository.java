package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
