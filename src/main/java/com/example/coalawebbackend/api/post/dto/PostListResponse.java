package com.example.coalawebbackend.api.post.dto;

import com.example.coalawebbackend.domain.post.entity.Post;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostListResponse {

    private Long postId;
    private Long boardId;
    private Long userId;
    private String title;

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getBoardId())
                .userId(post.getUser().getId())
                .title(post.getTitle())
                .build();
    }
}
