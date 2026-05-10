package com.example.coalawebbackend.api.services.dto;

public record ServiceInquiryResponse(
        String id,
        String title,
        String summary,
        String author,
        String createdAt,
        String status,
        String statusClass
) {
}
