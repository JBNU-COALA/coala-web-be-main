package com.example.coalawebbackend.api.attachment.dto;

import org.springframework.core.io.Resource;

public record AttachmentDownloadResponse(
        Resource resource,
        String originalName,
        String contentType,
        long fileSize
) {
}
