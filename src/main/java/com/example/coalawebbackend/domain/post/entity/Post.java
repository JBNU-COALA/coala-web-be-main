package com.example.coalawebbackend.domain.post.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private PostStatus status = PostStatus.ACTIVE;

    @Builder.Default
    @Column(name = "is_notice")
    private boolean notice = false;

    @Builder.Default
    @Column(name = "is_locked")
    private boolean locked = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    @Column(name = "delete_reason", length = 500)
    private String deleteReason;

    @Column(name = "thumbnail_attachment_id")
    private Long thumbnailAttachmentId;

    public static Post create(String title, String content,
                              Board board,
                              User user) {
        return Post.builder()
                .title(title)
                .content(content)
                .board(board)
                .user(user)
                .viewCount(0)
                .build();
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateBoard(Board board) {
        this.board = board;
    }

    public void updateThumbnailAttachmentId(Long thumbnailAttachmentId) {
        this.thumbnailAttachmentId = thumbnailAttachmentId;
    }

    public boolean isVisible() {
        return getStatus() == PostStatus.ACTIVE;
    }

    public boolean isMutable() {
        return getStatus() == PostStatus.ACTIVE && !locked;
    }

    public PostStatus getStatus() {
        return status == null ? PostStatus.ACTIVE : status;
    }

    public void softDelete(User actor, String reason, boolean adminDelete) {
        this.status = adminDelete ? PostStatus.ADMIN_DELETED : PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = actor;
        this.deleteReason = reason;
    }

    public void hide() {
        this.status = PostStatus.HIDDEN;
    }

    public void block() {
        this.status = PostStatus.BLOCKED;
    }

    public void restore() {
        this.status = PostStatus.ACTIVE;
        this.deletedAt = null;
        this.deletedBy = null;
        this.deleteReason = null;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }
}
