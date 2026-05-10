package com.example.coalawebbackend.api.moderation.controller;

import com.example.coalawebbackend.api.moderation.dto.CreateReportRequest;
import com.example.coalawebbackend.api.moderation.dto.ReportResponse;
import com.example.coalawebbackend.api.moderation.service.ModerationService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ModerationService moderationService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateReportRequest request
    ) {
        User reporter = userService.findById(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(moderationService.createReport(reporter, request));
    }
}
