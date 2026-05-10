package com.example.coalawebbackend.domain.moderation.service;

import com.example.coalawebbackend.domain.moderation.entity.AdminActionLog;
import com.example.coalawebbackend.domain.moderation.entity.AdminActionType;
import com.example.coalawebbackend.domain.moderation.entity.ModerationTargetType;
import com.example.coalawebbackend.domain.moderation.repository.AdminActionLogRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminActionLogRepository adminActionLogRepository;

    public void log(
            User admin,
            ModerationTargetType targetType,
            Long targetId,
            AdminActionType action,
            String reason,
            HttpServletRequest request
    ) {
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .targetType(targetType)
                .targetId(targetId)
                .action(action)
                .reason(reason == null || reason.isBlank() ? "사유 미입력" : reason.trim())
                .ipAddress(request == null ? null : request.getRemoteAddr())
                .userAgent(request == null ? null : request.getHeader("User-Agent"))
                .build());
    }
}
