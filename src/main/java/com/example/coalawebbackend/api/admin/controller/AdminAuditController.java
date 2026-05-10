package com.example.coalawebbackend.api.admin.controller;

import com.example.coalawebbackend.api.admin.dto.AdminActionLogResponse;
import com.example.coalawebbackend.domain.moderation.repository.AdminActionLogRepository;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditController {

    private final AdminActionLogRepository adminActionLogRepository;
    private final UserService userService;
    private final PermissionService permissionService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<AdminActionLogResponse>> getAuditLogs(
            @AuthenticationPrincipal String adminId
    ) {
        permissionService.assertModerator(userService.findById(adminId));
        return ResponseEntity.ok(adminActionLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(AdminActionLogResponse::from)
                .toList());
    }
}
