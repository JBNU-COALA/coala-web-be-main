package com.example.coalawebbackend.api.admin.dto;

import com.example.coalawebbackend.domain.moderation.entity.AdminActionLog;
import com.example.coalawebbackend.domain.moderation.entity.AdminActionType;
import com.example.coalawebbackend.domain.moderation.entity.ModerationTargetType;
import java.time.LocalDateTime;

public record AdminActionLogResponse(
        Long id,
        Long adminId,
        String adminName,
        ModerationTargetType targetType,
        Long targetId,
        AdminActionType action,
        String reason,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {
    public static AdminActionLogResponse from(AdminActionLog log) {
        return new AdminActionLogResponse(
                log.getId(),
                log.getAdmin().getId(),
                log.getAdmin().getName(),
                log.getTargetType(),
                log.getTargetId(),
                log.getAction(),
                log.getReason(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }
}
