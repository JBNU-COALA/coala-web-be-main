package com.example.coalawebbackend.api.services.dto;

import com.example.coalawebbackend.domain.instance.entity.ServiceInquiry;
import java.time.format.DateTimeFormatter;

public record ServiceInquiryResponse(
        String id,
        String title,
        String summary,
        String author,
        String createdAt,
        String status,
        String statusClass,
        String content,
        String reply,
        String answeredAt,
        Long authorId
) {
    public static ServiceInquiryResponse from(ServiceInquiry inquiry) {
        return new ServiceInquiryResponse(inquiry.getId(), inquiry.getTitle(), inquiry.getSummary(), inquiry.getAuthor(),
                inquiry.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")), inquiry.getStatus(),
                inquiry.getStatusClass(), inquiry.getContent(), inquiry.getReply() == null ? "" : inquiry.getReply(),
                inquiry.getAnsweredAt() == null ? null : inquiry.getAnsweredAt().toString(),
                inquiry.getUser() == null ? null : inquiry.getUser().getId());
    }
}
