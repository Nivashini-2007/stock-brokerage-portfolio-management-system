package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.NotificationResponse;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    /**
     * Persists (and, in a real deployment, would dispatch via SMS/email/
     * push) a notification for a client. This implementation only persists
     * and logs - see PersistedNotificationServiceImpl for the documented
     * swap point for a real gateway.
     */
    void notify(User client, NotificationType type, String message);

    List<NotificationResponse> getNotifications(Long clientId);
}
