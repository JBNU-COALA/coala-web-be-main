package com.example.coalawebbackend.api.comment.dto;

import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.entity.CommentStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse {

    private Long commentId;
    private Long parentCommentId;
    private Long userId;           // 익명 게시판에서는 본인 댓글이 아닌 경우 null
    private String authorName;
    private boolean anonymous;
    private boolean mine;
    private String content;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<CommentResponse> replies = List.of();

    public static CommentResponse from(
            Comment comment,
            List<CommentResponse> replies,
            String displayName,
            boolean anonymous,
            boolean mine
    ) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .parentCommentId(comment.getParent() == null ? null : comment.getParent().getId())
                .userId(anonymous && !mine ? null : comment.getUser().getId())
                .authorName(displayName)
                .anonymous(anonymous)
                .mine(mine)
                .content(displayContent(comment))
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replies)
                .build();
    }

    private static String displayContent(Comment comment) {
        return comment.isVisible() ? comment.getContent() : "삭제된 댓글입니다.";
    }
}
