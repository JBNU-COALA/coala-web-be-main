package com.example.coalawebbackend.api.notification.service;

import com.example.coalawebbackend.api.notification.dto.NotificationResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.notification.entity.Notification;
import com.example.coalawebbackend.domain.notification.entity.NotificationType;
import com.example.coalawebbackend.domain.notification.repository.NotificationRepository;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotifications(User user) {
        return notificationRepository.findTop30ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndReadAtIsNull(user);
    }

    @Transactional
    public NotificationResponse markRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        notification.markRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllRead(User user) {
        notificationRepository.findByUserAndReadAtIsNull(user)
                .forEach(Notification::markRead);
    }

    @Transactional
    public void notifyCommentCreated(Post post, Comment comment) {
        User actor = comment.getUser();
        Set<Long> notifiedUserIds = new HashSet<>();
        String actorName = displayName(actor);
        String linkUrl = "/community/board/%d/posts/%d".formatted(
                post.getBoard().getBoardId(),
                post.getPostId());

        notifyCommentRecipient(
                post.getUser(),
                actor,
                notifiedUserIds,
                "새 댓글",
                "%s님이 \"%s\" 글에 댓글을 남겼습니다.".formatted(actorName, post.getTitle()),
                linkUrl);

        if (comment.getParent() != null) {
            notifyCommentRecipient(
                    comment.getParent().getUser(),
                    actor,
                    notifiedUserIds,
                    "새 답글",
                    "%s님이 회원님의 댓글에 답글을 남겼습니다.".formatted(actorName),
                    linkUrl);
        }
    }

    @Transactional
    public void notifyInterestedInfo(User actor, InfoArticle article) {
        String linkUrl = "/community/info";
        String message = "새 정보공유 글 \"%s\"이 등록되었습니다.".formatted(article.getTitle());
        userRepository.findByVerifiedTrue().stream()
                .filter(user -> actor == null || !user.getId().equals(actor.getId()))
                .forEach(user -> notificationRepository.save(Notification.create(
                        user,
                        NotificationType.INTERESTED_INFO,
                        "관심정보",
                        message,
                        linkUrl)));
    }

    private void notifyCommentRecipient(
            User recipient,
            User actor,
            Set<Long> notifiedUserIds,
            String title,
            String message,
            String linkUrl) {
        if (recipient == null || actor == null || recipient.getId().equals(actor.getId())) {
            return;
        }
        if (!notifiedUserIds.add(recipient.getId())) {
            return;
        }
        notificationRepository.save(Notification.create(
                recipient,
                NotificationType.COMMENT,
                title,
                message,
                linkUrl));
    }

    private String displayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getName();
    }
}
