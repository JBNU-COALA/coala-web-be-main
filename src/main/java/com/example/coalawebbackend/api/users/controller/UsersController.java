package com.example.coalawebbackend.api.users.controller;

import com.example.coalawebbackend.api.users.dto.UserDirectoryResponse;
import com.example.coalawebbackend.api.users.service.UserDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users API", description = "유저 목록 및 프로필 API")
public class UsersController {

    private final UserDirectoryService userDirectoryService;

    @GetMapping
    @Operation(summary = "유저 목록 조회", description = "더미 데이터를 기반으로 유저 목록을 조회합니다.")
    public ResponseEntity<List<UserDirectoryResponse>> getUsers() {
        return ResponseEntity.ok(userDirectoryService.getUsers());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "유저 상세 조회", description = "유저 ID로 상세 프로필을 조회합니다.")
    public ResponseEntity<UserDirectoryResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userDirectoryService.getUser(userId));
    }
}
