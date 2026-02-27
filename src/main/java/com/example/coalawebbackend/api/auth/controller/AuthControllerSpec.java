package com.example.coalawebbackend.api.auth.controller;

import com.example.coalawebbackend.api.auth.dto.LoginRequest;
import com.example.coalawebbackend.api.auth.dto.TokenRefreshRequest;
import com.example.coalawebbackend.api.auth.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth API", description = "인증 관련 API (로그인, 토큰 갱신)")
public interface AuthControllerSpec {

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하여 액세스/리프레시 토큰을 발급합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 오류", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = LoginRequest.class)))
    ResponseEntity<TokenResponse> login(LoginRequest request);

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 액세스/리프레시 토큰을 발급합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "토큰 갱신 성공",
                content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = TokenRefreshRequest.class)))
    ResponseEntity<TokenResponse> refresh(TokenRefreshRequest request);
}
