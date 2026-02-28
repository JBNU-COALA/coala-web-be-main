package com.example.coalawebbackend.api.post.dto;

import com.example.coalawebbackend.domain.post.entity.Post;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostDetailResponse {

    private Long postId;
    private Long boardId;
    private Long userId;

    private String title;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getBoardId())
                .userId(post.getUser().getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
