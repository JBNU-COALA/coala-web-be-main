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
public class UpdatePostResponse {

    private Long postId;
    private String title;
    private String content;
    private LocalDateTime updatedAt;

    public static UpdatePostResponse from(Post post) {
        return UpdatePostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
