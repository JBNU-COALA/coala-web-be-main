package com.example.coalawebbackend.api.services.dto;

public record DomainApplicationResponse(
        String id,
        Long userId,
        String applicantName,
        String studentId,
        String contactEmail,
        String serviceName,
        String desiredAddress,
        String requestedDomain,
        String repositoryUrl,
        String targetUrl,
        String purpose,
        String requestedAt,
        String processedAt,
        String status,
        String adminNote
) {
}
