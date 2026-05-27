package com.example.coalawebbackend.api.info.dto;

import java.util.List;

public record InfoArticleResponse(
        Long id,
        String filter,
        String tag,
        String title,
        String meta,
        String source,
        String sourceName,
        Long authorId,
        String authorName,
        String sourceDate,
        String content,
        String imageUrl,
        List<Long> attachmentIds,
        Long thumbnailAttachmentId,
        long viewCount,
        long bookmarkCount,
        long likeCount,
        boolean likedByMe,
        String createdAt,
        String updatedAt
) {
}
