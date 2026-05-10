package com.example.coalawebbackend.api.moderation.dto;

import com.example.coalawebbackend.domain.moderation.entity.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HandleReportRequest(
        @NotNull ReportStatus status,
        @NotBlank @Size(max = 500) String reason
) {
}
