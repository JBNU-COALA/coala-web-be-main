package com.example.coalawebbackend.api.services.dto;

import java.util.List;

public record InstanceApplicationResponse(
        String id,
        String applicantName,
        String studentId,
        String keyEmail,
        String instanceType,
        String purpose,
        String duration,
        String requestedAt,
        String approvedAt,
        String status,
        String adminNote,
        List<AttachedFileResponse> attachedFiles,
        InstanceSpecResponse specs
) {

    public record AttachedFileResponse(
            String name,
            String size,
            String uploadedAt
    ) {
    }

    public record InstanceSpecResponse(
            String cpu,
            String ram,
            String disk
    ) {
    }
}
