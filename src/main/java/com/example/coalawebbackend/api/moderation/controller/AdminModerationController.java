package com.example.coalawebbackend.api.moderation.controller;

import com.example.coalawebbackend.api.moderation.dto.AdminReasonRequest;
import com.example.coalawebbackend.api.moderation.dto.HandleReportRequest;
import com.example.coalawebbackend.api.moderation.dto.ReportResponse;
import com.example.coalawebbackend.api.moderation.dto.UserSanctionRequest;
import com.example.coalawebbackend.api.moderation.service.ModerationService;
import com.example.coalawebbackend.domain.moderation.entity.ReportStatus;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/moderation")
public class AdminModerationController {

    private final ModerationService moderationService;
    private final UserService userService;

    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> getReports(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "PENDING") ReportStatus status
    ) {
        User admin = userService.findById(userId);
        return ResponseEntity.ok(moderationService.getReports(admin, status));
    }

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<ReportResponse> handleReport(
            @AuthenticationPrincipal String userId,
            @PathVariable Long reportId,
            @Valid @RequestBody HandleReportRequest body,
            HttpServletRequest request
    ) {
        User admin = userService.findById(userId);
        return ResponseEntity.ok(moderationService.handleReport(
                admin, reportId, body.status(), body.reason(), request));
    }

    @PostMapping("/posts/{postId}/hide")
    public ResponseEntity<Void> hidePost(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.hidePost(userService.findById(userId), postId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/restore")
    public ResponseEntity<Void> restorePost(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.restorePost(userService.findById(userId), postId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/delete")
    public ResponseEntity<Void> adminDeletePost(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.adminDeletePost(userService.findById(userId), postId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/lock")
    public ResponseEntity<Void> lockPost(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.lockPost(userService.findById(userId), postId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/unlock")
    public ResponseEntity<Void> unlockPost(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.unlockPost(userService.findById(userId), postId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/hide")
    public ResponseEntity<Void> hideComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.hideComment(userService.findById(userId), commentId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/restore")
    public ResponseEntity<Void> restoreComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.restoreComment(userService.findById(userId), commentId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/delete")
    public ResponseEntity<Void> adminDeleteComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @Valid @RequestBody AdminReasonRequest body,
            HttpServletRequest request
    ) {
        moderationService.adminDeleteComment(userService.findById(userId), commentId, body.reason(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sanctions")
    public ResponseEntity<Void> sanctionUser(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UserSanctionRequest body,
            HttpServletRequest request
    ) {
        moderationService.sanctionUser(userService.findById(userId), body, request);
        return ResponseEntity.noContent().build();
    }
}
