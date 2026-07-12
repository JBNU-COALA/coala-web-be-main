package com.example.coalawebbackend.api.comment.dto;

import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.entity.CommentStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCommentResponse {

    private Long commentId;
    private Long parentCommentId;
    private Long userId;
    private String authorName;
    private boolean anonymous;
    private String content;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CreateCommentResponse from(Comment comment, String displayName, boolean anonymous) {
        return CreateCommentResponse.builder()
                .commentId(comment.getId())
                .parentCommentId(comment.getParent() == null ? null : comment.getParent().getId())
                .userId(anonymous ? null : comment.getUser().getId())
                .authorName(displayName)
                .anonymous(anonymous)
                .content(comment.getContent())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
