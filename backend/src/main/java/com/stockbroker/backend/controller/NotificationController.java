package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.NotificationResponse;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{clientId}")
    public List<NotificationResponse> getNotifications(@PathVariable Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);
        return notificationService.getNotifications(clientId);
    }
}
