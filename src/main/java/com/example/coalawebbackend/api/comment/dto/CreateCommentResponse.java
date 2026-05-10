package com.example.coalawebbackend.api.comment.dto;

import com.example.coalawebbackend.domain.comment.entity.Comment;
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
    private String content;
    private LocalDateTime createdAt;

    public static CreateCommentResponse from(Comment comment) {
        return CreateCommentResponse.builder()
                .commentId(comment.getId())
                .parentCommentId(comment.getParent() == null ? null : comment.getParent().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
