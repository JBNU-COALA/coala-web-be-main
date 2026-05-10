package com.example.coalawebbackend.api.moderation.dto;

import com.example.coalawebbackend.domain.moderation.entity.ModerationTargetType;
import com.example.coalawebbackend.domain.moderation.entity.ReportReasonType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotNull ModerationTargetType targetType,
        @NotNull Long targetId,
        @NotNull ReportReasonType reasonType,
        @Size(max = 1000) String reasonDetail
) {
}
