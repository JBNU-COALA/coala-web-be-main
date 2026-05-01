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
public class PostListResponse {

    private Long postId;
    private Long boardId;
    private String boardName;
    private Long userId;
    private String authorName;     // 작성자 표시명 (nickname 우선, 없으면 name)
    private String title;
    private String content;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostListResponse from(Post post) {
        String nickname = post.getUser().getNickname();
        String displayName = (nickname != null && !nickname.isBlank())
                ? nickname
                : post.getUser().getName();

        return PostListResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getBoardId())
                .boardName(post.getBoard().getName())
                .userId(post.getUser().getId())
                .authorName(displayName)
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
