package com.example.coalawebbackend.api.notification.dto;

import com.example.coalawebbackend.domain.notification.entity.Notification;
import com.example.coalawebbackend.domain.notification.entity.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String linkUrl,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getLinkUrl(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt());
    }
}
