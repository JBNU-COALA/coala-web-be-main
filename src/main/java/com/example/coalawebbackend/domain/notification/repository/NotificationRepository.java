package com.example.coalawebbackend.domain.notification.repository;

import com.example.coalawebbackend.domain.notification.entity.Notification;
import com.example.coalawebbackend.domain.notification.entity.NotificationType;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop30ByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndReadAtIsNull(User user);

    Optional<Notification> findByIdAndUser(Long id, User user);

    long countByUserAndReadAtIsNull(User user);

    boolean existsByUserAndTypeAndTitleAndLinkUrl(User user, NotificationType type, String title, String linkUrl);
}
