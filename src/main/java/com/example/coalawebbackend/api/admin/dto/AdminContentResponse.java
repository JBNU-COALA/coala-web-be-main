package com.example.coalawebbackend.api.admin.dto;

import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.entity.PostStatus;
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.user.entity.User;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;

public record AdminContentResponse(
        String contentType,
        String contentKey,
        Long postId,
        Long boardId,
        String boardName,
        Long userId,
        String authorName,
        String title,
        String content,
        PostStatus status,
        boolean locked,
        boolean notice,
        int viewCount,
        long commentCount,
        long likeCount,
        Long thumbnailAttachmentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String externalId
) {

    public static AdminContentResponse fromPost(Post post) {
        return new AdminContentResponse(
                "POST",
                "post-" + post.getPostId(),
                post.getPostId(),
                post.getBoard().getBoardId(),
                post.getBoard().getName(),
                post.getUser().getId(),
                displayName(post.getUser()),
                post.getTitle(),
                post.getContent(),
                post.getStatus(),
                post.isLocked(),
                post.isNotice(),
                post.getViewCount(),
                0,
                0,
                post.getThumbnailAttachmentId(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                null
        );
    }

    public static AdminContentResponse fromInfo(InfoArticle article) {
        String category = article.getCategory().getApiValue();
        User author = article.getAuthor();
        String authorName = author == null
                ? (StringUtils.hasText(article.getSourceName()) ? article.getSourceName() : "코알라")
                : displayName(author);
        return new AdminContentResponse(
                "INFO",
                "info-" + article.getId(),
                article.getId(),
                null,
                "정보공유 · " + category,
                author == null ? null : author.getId(),
                authorName,
                article.getTitle(),
                article.getContent(),
                PostStatus.ACTIVE,
                false,
                false,
                Math.toIntExact(Math.min(Integer.MAX_VALUE, article.getViewCount())),
                0,
                article.getBookmarkCount(),
                null,
                article.getCreatedAt(),
                article.getUpdatedAt(),
                String.valueOf(article.getId())
        );
    }

    public static AdminContentResponse fromRecruit(RecruitPost recruit) {
        User author = recruit.getAuthor();
        return new AdminContentResponse(
                "RECRUIT",
                "recruit-" + recruit.getId(),
                null,
                null,
                "모집 · " + recruit.getCategory(),
                author == null ? null : author.getId(),
                author == null ? recruit.getHost() : displayName(author),
                recruit.getTitle(),
                String.join("\n\n", recruit.getDetailContent()),
                PostStatus.ACTIVE,
                false,
                false,
                Math.toIntExact(Math.min(Integer.MAX_VALUE, recruit.getViews())),
                recruit.getRoles().size(),
                recruit.getBookmarks(),
                null,
                recruit.getCreatedAt(),
                recruit.getUpdatedAt(),
                recruit.getId()
        );
    }

    private static String displayName(User user) {
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getName();
    }
}
