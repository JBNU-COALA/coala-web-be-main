package com.example.coalawebbackend.api.recruit.controller;

import com.example.coalawebbackend.api.recruit.dto.RecruitApplicationRequest;
import com.example.coalawebbackend.api.recruit.dto.RecruitApplicationResponse;
import com.example.coalawebbackend.api.recruit.dto.RecruitCommentRequest;
import com.example.coalawebbackend.api.recruit.dto.RecruitCommentResponse;
import com.example.coalawebbackend.api.recruit.dto.RecruitPostRequest;
import com.example.coalawebbackend.api.recruit.dto.RecruitPostResponse;
import com.example.coalawebbackend.api.recruit.service.RecruitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruits")
@Tag(name = "Recruit API", description = "모집 공고, 지원서, Q&A API")
public class RecruitController {

    private final RecruitService recruitService;

    @GetMapping
    @Operation(summary = "모집 공고 목록 조회", description = "모집 공고 목록을 필터링해서 조회합니다.")
    public ResponseEntity<List<RecruitPostResponse>> getRecruits(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(recruitService.getRecruits(category, status, query, sort));
    }

    @GetMapping("/{recruitId}")
    @Operation(summary = "모집 공고 상세 조회", description = "모집 공고 상세를 조회하고 조회수를 증가시킵니다.")
    public ResponseEntity<RecruitPostResponse> getRecruit(@PathVariable String recruitId) {
        return ResponseEntity.ok(recruitService.getRecruit(recruitId));
    }

    @PostMapping
    @Operation(summary = "모집 공고 생성", description = "모집 공고를 생성합니다.")
    public ResponseEntity<RecruitPostResponse> createRecruit(
            @Valid @RequestBody RecruitPostRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitService.createRecruit(request, userId));
    }

    @GetMapping("/{recruitId}/comments")
    @Operation(summary = "모집 Q&A 댓글 조회", description = "모집 공고의 Q&A 댓글을 조회합니다.")
    public ResponseEntity<List<RecruitCommentResponse>> getComments(@PathVariable String recruitId) {
        return ResponseEntity.ok(recruitService.getComments(recruitId));
    }

    @PostMapping("/{recruitId}/comments")
    @Operation(summary = "모집 Q&A 댓글 생성", description = "모집 공고에 질문/답변 댓글을 작성합니다.")
    public ResponseEntity<RecruitCommentResponse> createComment(
            @PathVariable String recruitId,
            @Valid @RequestBody RecruitCommentRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitService.createComment(recruitId, request, userId));
    }

    @PostMapping("/{recruitId}/applications")
    @Operation(summary = "모집 지원서 제출", description = "모집 공고에 지원서를 제출합니다.")
    public ResponseEntity<RecruitApplicationResponse> apply(
            @PathVariable String recruitId,
            @Valid @RequestBody RecruitApplicationRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitService.apply(recruitId, request, userId));
    }

    @GetMapping("/applications/me")
    @Operation(summary = "내 모집 지원서 조회", description = "로그인 사용자의 모집 지원서 목록을 조회합니다.")
    public ResponseEntity<List<RecruitApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(recruitService.getMyApplications(userId));
    }

    @GetMapping("/{recruitId}/applications")
    @Operation(summary = "모집 공고 지원서 조회", description = "특정 모집 공고의 지원서 목록을 조회합니다.")
    public ResponseEntity<List<RecruitApplicationResponse>> getRecruitApplications(@PathVariable String recruitId) {
        return ResponseEntity.ok(recruitService.getRecruitApplications(recruitId));
    }

    @PostMapping("/{recruitId}/bookmarks")
    @Operation(summary = "모집 관심 저장", description = "모집 공고 관심 수를 증가시킵니다.")
    public ResponseEntity<RecruitPostResponse> bookmark(@PathVariable String recruitId) {
        return ResponseEntity.ok(recruitService.bookmark(recruitId));
    }
}
