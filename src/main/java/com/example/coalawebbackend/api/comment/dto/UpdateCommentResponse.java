package com.example.coalawebbackend.api.comment.dto;

import com.example.coalawebbackend.domain.comment.entity.Comment;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCommentResponse {

    private Long commentId;
    private String content;
    private LocalDateTime updatedAt;

    public static UpdateCommentResponse of(Comment comment) {
        return new UpdateCommentResponse(comment.getId(),comment.getContent(),comment.getUpdatedAt());
    }
}
