package com.example.coalawebbackend.api.attachment.dto;

import com.example.coalawebbackend.domain.attachment.entity.Attachment;

public record AttachmentUploadResponse(
        Long attachmentId,
        String originalName,
        String contentType,
        long fileSize,
        String url,
        String status
) {

    public static AttachmentUploadResponse from(Attachment attachment) {
        return new AttachmentUploadResponse(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                "/media/attachments/" + attachment.getId() + "/download",
                attachment.getStatus().name()
        );
    }
}
