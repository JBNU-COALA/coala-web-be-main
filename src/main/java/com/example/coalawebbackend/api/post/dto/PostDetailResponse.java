package com.example.coalawebbackend.api.post.dto;

import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.entity.PostStatus;
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
    private String boardName;
    private Long userId;           // 익명 게시판에서는 본인 글이 아닌 경우 null
    private String authorName;
    private boolean anonymous;
    private boolean mine;

    private String title;
    private String content;
    private PostStatus status;
    private boolean locked;
    private boolean notice;
    private int viewCount;
    private long commentCount;
    private long likeCount;
    private boolean likedByMe;
    private Long thumbnailAttachmentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDetailResponse from(
            Post post,
            long commentCount,
            long likeCount,
            boolean likedByMe,
            String displayName,
            boolean anonymous,
            boolean mine
    ) {
        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getBoardId())
                .boardName(post.getBoard().getName())
                .userId(anonymous && !mine ? null : post.getUser().getId())
                .authorName(displayName)
                .anonymous(anonymous)
                .mine(mine)
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .locked(post.isLocked())
                .notice(post.isNotice())
                .viewCount(post.getViewCount())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .thumbnailAttachmentId(post.getThumbnailAttachmentId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
