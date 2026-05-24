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
        String sourceDate,
        String content,
        String imageUrl,
        List<Long> attachmentIds,
        Long thumbnailAttachmentId,
        long viewCount,
        long bookmarkCount,
        String createdAt,
        String updatedAt
) {
}
