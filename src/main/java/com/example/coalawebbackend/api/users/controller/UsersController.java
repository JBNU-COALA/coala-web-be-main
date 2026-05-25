package com.example.coalawebbackend.api.users.controller;

import com.example.coalawebbackend.api.users.dto.UserActivityItemResponse;
import com.example.coalawebbackend.api.users.dto.UserDirectoryResponse;
import com.example.coalawebbackend.api.users.dto.UserProfileRequest;
import com.example.coalawebbackend.api.users.service.UserActivityService;
import com.example.coalawebbackend.api.users.service.UserDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users API", description = "유저 목록 및 프로필 API")
public class UsersController {

    private final UserDirectoryService userDirectoryService;
    private final UserActivityService userActivityService;

    @GetMapping
    @Operation(summary = "유저 목록 조회", description = "가입된 사용자 계정을 기반으로 유저 목록을 조회합니다.")
    public ResponseEntity<List<UserDirectoryResponse>> getUsers(@AuthenticationPrincipal String currentUserId) {
        return ResponseEntity.ok(userDirectoryService.getUsers(parseUserId(currentUserId)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "유저 상세 조회", description = "유저 ID로 상세 프로필을 조회합니다.")
    public ResponseEntity<UserDirectoryResponse> getUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal String currentUserId
    ) {
        return ResponseEntity.ok(userDirectoryService.getUser(userId, parseUserId(currentUserId)));
    }

    @GetMapping("/me/activity")
    @Operation(summary = "내 작성/신청 활동 조회", description = "게시판, 정보공유, 모집, 인스턴스 신청, 도메인 신청을 한 번에 조회합니다.")
    public ResponseEntity<List<UserActivityItemResponse>> getMyActivity(
            @AuthenticationPrincipal String currentUserId
    ) {
        return ResponseEntity.ok(userActivityService.getMyActivities(parseUserId(currentUserId)));
    }

    @PatchMapping("/me/profile")
    @Operation(summary = "내 프로필 편집", description = "마이프로필의 소개, 활동 내역, 수상 내역, 공유 저장소를 수정합니다.")
    public ResponseEntity<UserDirectoryResponse> updateMyProfile(
            @AuthenticationPrincipal String currentUserId,
            @Valid @RequestBody UserProfileRequest request
    ) {
        return ResponseEntity.ok(userDirectoryService.updateMyProfile(parseUserId(currentUserId), request));
    }

    private Long parseUserId(String currentUserId) {
        if (currentUserId == null || currentUserId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(currentUserId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
