package com.example.coalawebbackend.api.notification.controller;

import com.example.coalawebbackend.api.notification.dto.NotificationResponse;
import com.example.coalawebbackend.api.notification.dto.UnreadNotificationCountResponse;
import com.example.coalawebbackend.api.notification.service.NotificationService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(notificationService.getNotifications(currentUser(userId)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadCount(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(new UnreadNotificationCountResponse(
                notificationService.countUnread(currentUser(userId))));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @AuthenticationPrincipal String userId,
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markRead(currentUser(userId), notificationId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal String userId) {
        notificationService.markAllRead(currentUser(userId));
        return ResponseEntity.noContent().build();
    }

    private User currentUser(String userId) {
        return userService.findById(userId);
    }
}
