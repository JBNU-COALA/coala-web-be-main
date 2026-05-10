package com.example.coalawebbackend.api.comment.dto;

import com.example.coalawebbackend.domain.comment.entity.Comment;
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
    private Long userId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<CommentResponse> replies = List.of();

    public static CommentResponse from(Comment comment) {
        return from(comment, List.of());
    }

    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        String nickname = comment.getUser().getNickname();
        String displayName = (nickname != null && !nickname.isBlank())
                ? nickname
                : comment.getUser().getName();

        return CommentResponse.builder()
                .commentId(comment.getId())
                .parentCommentId(comment.getParent() == null ? null : comment.getParent().getId())
                .userId(comment.getUser().getId())
                .authorName(displayName)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replies)
                .build();
    }
}
