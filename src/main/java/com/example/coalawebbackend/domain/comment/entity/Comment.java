package com.example.coalawebbackend.domain.comment.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private CommentStatus status = CommentStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    @Column(name = "delete_reason", length = 500)
    private String deleteReason;

    public static Comment create(Post post, User user, String content) {
        Comment comment = new Comment();
        comment.post = post;
        comment.user = user;
        comment.content = content;
        return comment;
    }

    public static Comment createReply(Post post, User user, Comment parent, String content) {
        Comment comment = create(post, user, content);
        comment.parent = parent;
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isVisible() {
        return getStatus() == CommentStatus.ACTIVE;
    }

    public boolean isMutable() {
        return getStatus() == CommentStatus.ACTIVE;
    }

    public CommentStatus getStatus() {
        return status == null ? CommentStatus.ACTIVE : status;
    }

    public void softDelete(User actor, String reason, boolean adminDelete) {
        this.status = adminDelete ? CommentStatus.ADMIN_DELETED : CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = actor;
        this.deleteReason = reason;
    }

    public void hide() {
        this.status = CommentStatus.HIDDEN;
    }

    public void restore() {
        this.status = CommentStatus.ACTIVE;
        this.deletedAt = null;
        this.deletedBy = null;
        this.deleteReason = null;
    }
}
