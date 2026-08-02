package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.Margin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarginRepository extends JpaRepository<Margin, Long> {

    Optional<Margin> findByClientId(Long clientId);

}