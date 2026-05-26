package com.example.coalawebbackend.api.services.dto;

import java.util.List;

public record MemberServiceResponse(
        String id,
        String title,
        String category,
        String owner,
        String summary,
        String url,
        String githubUrl,
        String imageUrl,
        List<String> additionalImageUrls,
        List<String> tags,
        String status,
        String audience,
        String visibility,
        String period,
        String description,
        List<String> features,
        List<String> stack,
        boolean canEdit
) {
}
