package com.example.coalawebbackend.api.recruit.dto;

public record RecruitCommentResponse(
        String id,
        String author,
        String authorInitials,
        String authorTone,
        String timeLabel,
        String content,
        String createdAt
) {
}
