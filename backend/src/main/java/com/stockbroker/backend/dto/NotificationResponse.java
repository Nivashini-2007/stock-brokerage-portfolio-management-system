package com.stockbroker.backend.dto;

import com.stockbroker.backend.enums.NotificationChannel;
import com.stockbroker.backend.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private NotificationChannel channel;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;
}
