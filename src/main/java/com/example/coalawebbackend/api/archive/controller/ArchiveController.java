package com.example.coalawebbackend.api.archive.controller;

import com.example.coalawebbackend.api.archive.dto.ArchiveItemRequest;
import com.example.coalawebbackend.api.archive.dto.ArchiveItemResponse;
import com.example.coalawebbackend.api.archive.service.ArchiveItemService;
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
@RequestMapping("/api/archive")
@Tag(name = "Archive API", description = "자료실 API")
public class ArchiveController {

    private final ArchiveItemService archiveItemService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "자료실 목록 조회", description = "연구실, 에이전트/스킬 자료를 조회합니다.")
    public ResponseEntity<List<ArchiveItemResponse>> getItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(archiveItemService.getItems(category, query));
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "자료실 상세 조회", description = "자료실 항목을 조회합니다.")
    public ResponseEntity<ArchiveItemResponse> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(archiveItemService.getItem(itemId));
    }

    @PostMapping
    @Operation(summary = "자료실 항목 생성", description = "자료실 항목을 생성합니다.")
    public ResponseEntity<ArchiveItemResponse> createItem(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ArchiveItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(archiveItemService.createItem(userService.findById(userId), request));
    }

    @PatchMapping("/{itemId}")
    @Operation(summary = "자료실 항목 수정", description = "작성자 또는 운영진이 자료실 항목을 수정합니다.")
    public ResponseEntity<ArchiveItemResponse> updateItem(
            @AuthenticationPrincipal String userId,
            @PathVariable Long itemId,
            @Valid @RequestBody ArchiveItemRequest request
    ) {
        return ResponseEntity.ok(archiveItemService.updateItem(userService.findById(userId), itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "자료실 항목 삭제", description = "작성자 또는 운영진이 자료실 항목을 삭제합니다.")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal String userId,
            @PathVariable Long itemId
    ) {
        archiveItemService.deleteItem(userService.findById(userId), itemId);
        return ResponseEntity.noContent().build();
    }
}
