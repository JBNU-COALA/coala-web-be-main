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
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.recruit.repository.RecruitBookmarkRepository;
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
    private final RecruitBookmarkRepository recruitBookmarkRepository;

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
                linkUrl,
                NotificationType.COMMENT);
    }

    @Transactional
    public void notifyReplyCreated(Post post, Comment parent, Comment reply) {
        User actor = reply.getUser();
        Set<Long> notifiedUserIds = new HashSet<>();
        String actorName = displayName(actor);
        String linkUrl = "/community/board/%d/posts/%d#comment-%d".formatted(
                post.getBoard().getBoardId(),
                post.getPostId(),
                parent.getId());

        notifyCommentRecipient(
                parent.getUser(),
                actor,
                notifiedUserIds,
                "새 답글",
                "%s님이 회원님의 댓글에 답글을 남겼습니다.".formatted(actorName),
                linkUrl,
                NotificationType.REPLY);

        notifyCommentRecipient(
                post.getUser(),
                actor,
                notifiedUserIds,
                "새 댓글",
                "%s님이 \"%s\" 글에 답글을 남겼습니다.".formatted(actorName, post.getTitle()),
                linkUrl,
                NotificationType.COMMENT);
    }

    @Transactional
    public void notifyInterestedInfo(User actor, InfoArticle article) {
        String linkUrl = infoArticleLink(article);
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

    @Transactional
    public void notifyPostLiked(Post post, User actor) {
        if (post == null || actor == null || post.getUser() == null || post.getUser().getId().equals(actor.getId())) {
            return;
        }
        notificationRepository.save(Notification.create(
                post.getUser(),
                NotificationType.LIKE,
                "새 좋아요",
                "%s님이 \"%s\" 글을 좋아합니다.".formatted(displayName(actor), post.getTitle()),
                "/community/board/%d/posts/%d".formatted(post.getBoard().getBoardId(), post.getPostId())));
    }

    @Transactional
    public void notifyInfoArticleLiked(InfoArticle article, User actor) {
        if (article == null || actor == null || article.getAuthor() == null || article.getAuthor().getId().equals(actor.getId())) {
            return;
        }
        notificationRepository.save(Notification.create(
                article.getAuthor(),
                NotificationType.LIKE,
                "새 좋아요",
                "%s님이 \"%s\" 정보공유 글을 좋아합니다.".formatted(displayName(actor), article.getTitle()),
                infoArticleLink(article)));
    }

    @Transactional
    public void notifyRecruitClosingSoon(RecruitPost recruit) {
        recruitBookmarkRepository.findByRecruitPost_Id(recruit.getId())
                .forEach(bookmark -> notifyRecruitClosingSoon(bookmark.getUser(), recruit));
    }

    @Transactional
    public void notifyRecruitClosingSoon(User user, RecruitPost recruit) {
        if (user == null || recruit == null) {
            return;
        }
        String title = "모집 마감 임박";
        String linkUrl = "/community/recruit/notices/" + recruit.getId();
        if (notificationRepository.existsByUserAndTypeAndTitleAndLinkUrl(user, NotificationType.RECRUIT, title, linkUrl)) {
            return;
        }
        notificationRepository.save(Notification.create(
                user,
                NotificationType.RECRUIT,
                title,
                "\"%s\" 모집이 곧 마감됩니다.".formatted(recruit.getTitle()),
                linkUrl));
    }

    @Transactional
    public void notifyRecruitCommentCreated(RecruitPost recruit, User actor) {
        if (recruit == null || recruit.getAuthor() == null || actor == null) {
            return;
        }
        if (recruit.getAuthor().getId().equals(actor.getId())) {
            return;
        }
        String actorName = displayName(actor);
        notificationRepository.save(Notification.create(
                recruit.getAuthor(),
                NotificationType.RECRUIT,
                "모집 문의",
                "%s님이 \"%s\" 모집에 문의를 남겼습니다.".formatted(actorName, recruit.getTitle()),
                "/community/recruit/notices/" + recruit.getId()));
    }

    private void notifyCommentRecipient(
            User recipient,
            User actor,
            Set<Long> notifiedUserIds,
            String title,
            String message,
            String linkUrl,
            NotificationType type) {
        if (recipient == null || actor == null || recipient.getId().equals(actor.getId())) {
            return;
        }
        if (!notifiedUserIds.add(recipient.getId())) {
            return;
        }
        notificationRepository.save(Notification.create(
                recipient,
                type,
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

    private String infoArticleLink(InfoArticle article) {
        return "/community/info/%d/posts/%d".formatted(infoBoardId(article), article.getId());
    }

    private int infoBoardId(InfoArticle article) {
        return switch (article.getCategory()) {
            case NEWS -> 11;
            case CONTEST -> 12;
            case LAB -> 13;
            case RESOURCE -> 14;
        };
    }
}
