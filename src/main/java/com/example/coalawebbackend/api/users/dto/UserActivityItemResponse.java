package com.example.coalawebbackend.api.users.dto;

public record UserActivityItemResponse(
        String id,
        String kind,
        String label,
        String title,
        String excerpt,
        String status,
        String category,
        Long boardId,
        Long postId,
        String externalId,
        Long viewCount,
        String createdAt
) {
}
