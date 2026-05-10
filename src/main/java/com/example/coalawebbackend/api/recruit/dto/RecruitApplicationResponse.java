package com.example.coalawebbackend.api.recruit.dto;

public record RecruitApplicationResponse(
        Long id,
        String recruitId,
        String recruitTitle,
        String role,
        String body,
        String submittedAt,
        String status
) {
}
