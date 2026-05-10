package com.example.coalawebbackend.api.site.dto;

import java.util.List;

public record SiteContentResponse(
        String title,
        String description,
        List<String> chips
) {
}
