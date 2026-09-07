package com.stockbroker.backend.repository;

import com.stockbroker.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
