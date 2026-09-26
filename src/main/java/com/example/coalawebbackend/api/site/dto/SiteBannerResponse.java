package com.example.coalawebbackend.api.site.dto;

public record SiteBannerResponse(
        Long id, String title, String eyebrow, String description, String imageUrl,
        String targetPath, String actionLabel, String tone, int sortOrder, boolean enabled
) {
}
