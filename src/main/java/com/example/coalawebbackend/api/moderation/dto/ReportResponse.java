package com.example.coalawebbackend.api.moderation.dto;

import com.example.coalawebbackend.domain.moderation.entity.ModerationTargetType;
import com.example.coalawebbackend.domain.moderation.entity.Report;
import com.example.coalawebbackend.domain.moderation.entity.ReportReasonType;
import com.example.coalawebbackend.domain.moderation.entity.ReportStatus;
import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long reporterId,
        ModerationTargetType targetType,
        Long targetId,
        ReportReasonType reasonType,
        String reasonDetail,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime handledAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReasonType(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getHandledAt());
    }
}
