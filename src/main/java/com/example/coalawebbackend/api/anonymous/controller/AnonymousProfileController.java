package com.example.coalawebbackend.api.anonymous.controller;

import com.example.coalawebbackend.api.anonymous.dto.AnonymousProfileResponse;
import com.example.coalawebbackend.api.anonymous.dto.AnonymousProfileUpdateRequest;
import com.example.coalawebbackend.api.anonymous.facade.AnonymousProfileFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 익명 게시판 내 "본인" 표시 프로필(익명 이름) 조회/수정.
 * 인증된 본인만 자신의 표시명을 볼 수 있고 수정할 수 있으며, 수정 시 해당 게시판의 기존 글/댓글에도 즉시 반영된다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/anonymous-profile")
public class AnonymousProfileController {

    private final AnonymousProfileFacade anonymousProfileFacade;

    @GetMapping
    public ResponseEntity<AnonymousProfileResponse> getMyProfile(
            @PathVariable Long boardId,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(anonymousProfileFacade.getMyProfile(boardId, userId));
    }

    @PutMapping
    public ResponseEntity<AnonymousProfileResponse> updateMyProfile(
            @PathVariable Long boardId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AnonymousProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(anonymousProfileFacade.updateMyProfile(boardId, userId, request));
    }
}
