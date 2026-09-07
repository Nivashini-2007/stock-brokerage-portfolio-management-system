package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.NotificationResponse;
import com.stockbroker.backend.entity.Notification;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.enums.NotificationChannel;
import com.stockbroker.backend.enums.NotificationType;
import com.stockbroker.backend.repository.NotificationRepository;
import com.stockbroker.backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Persists notifications and logs them. There is no reachable SMS/email/
 * push gateway in this environment, so IN_APP (persisted + visible via
 * GET /api/notifications/{clientId}) is the only channel actually
 * delivered - swap this class for one that also calls a real gateway to
 * go live, without changing any calling code.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notify(User client, NotificationType type, String message) {

        Notification notification = new Notification();
        notification.setClient(client);
        notification.setType(type);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setMessage(message);

        notificationRepository.save(notification);

        log.info("Notification [{}] for client {}: {}",
                type, client != null ? client.getId() : "ALL", message);
    }

    @Override
    public List<NotificationResponse> getNotifications(Long clientId) {

        return notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse mapToResponse(Notification notification) {

        NotificationResponse response = new NotificationResponse();

        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setChannel(notification.getChannel());
        response.setMessage(notification.getMessage());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());

        return response;
    }
}
