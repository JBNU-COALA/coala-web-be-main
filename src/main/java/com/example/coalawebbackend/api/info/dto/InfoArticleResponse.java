package com.example.coalawebbackend.api.info.dto;

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
        long viewCount,
        long bookmarkCount,
        String createdAt,
        String updatedAt
) {
}
