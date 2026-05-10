package com.example.coalawebbackend.api.info.controller;

import com.example.coalawebbackend.api.info.dto.InfoArticleRequest;
import com.example.coalawebbackend.api.info.dto.InfoArticleResponse;
import com.example.coalawebbackend.api.info.service.InfoArticleService;
import com.example.coalawebbackend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/info")
@Tag(name = "Info API", description = "정보공유 글 API")
public class InfoArticleController {

    private final InfoArticleService infoArticleService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "정보공유 목록 조회", description = "소식/대회/연구실/자료 글을 조회합니다.")
    public ResponseEntity<List<InfoArticleResponse>> getArticles(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(infoArticleService.getArticles(filter, query));
    }

    @GetMapping("/{articleId}")
    @Operation(summary = "정보공유 상세 조회", description = "정보공유 글 상세를 조회하고 조회수를 증가시킵니다.")
    public ResponseEntity<InfoArticleResponse> getArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(infoArticleService.getArticle(articleId));
    }

    @PostMapping
    @Operation(summary = "정보공유 글 생성", description = "정보공유 글을 생성합니다.")
    public ResponseEntity<InfoArticleResponse> createArticle(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody InfoArticleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(infoArticleService.createArticle(userService.findById(userId), request));
    }

    @PatchMapping("/{articleId}")
    @Operation(summary = "정보공유 글 수정", description = "정보공유 글을 수정합니다.")
    public ResponseEntity<InfoArticleResponse> updateArticle(
            @AuthenticationPrincipal String userId,
            @PathVariable Long articleId,
            @Valid @RequestBody InfoArticleRequest request
    ) {
        return ResponseEntity.ok(infoArticleService.updateArticle(userService.findById(userId), articleId, request));
    }

    @DeleteMapping("/{articleId}")
    @Operation(summary = "정보공유 글 삭제", description = "정보공유 글을 삭제합니다.")
    public ResponseEntity<Void> deleteArticle(
            @AuthenticationPrincipal String userId,
            @PathVariable Long articleId
    ) {
        infoArticleService.deleteArticle(userService.findById(userId), articleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{articleId}/bookmarks")
    @Operation(summary = "정보공유 저장", description = "정보공유 글의 저장 수를 증가시킵니다.")
    public ResponseEntity<InfoArticleResponse> bookmarkArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(infoArticleService.bookmarkArticle(articleId));
    }
}
