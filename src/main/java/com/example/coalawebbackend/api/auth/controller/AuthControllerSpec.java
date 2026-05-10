package com.example.coalawebbackend.api.auth.controller;

import com.example.coalawebbackend.api.auth.dto.EmailVerificationConfirmRequest;
import com.example.coalawebbackend.api.auth.dto.EmailVerificationResendRequest;
import com.example.coalawebbackend.api.auth.dto.EmailVerificationResponse;
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
    ResponseEntity<TokenResponse> login(
            LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest);

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

    @Operation(summary = "로그아웃", description = "액세스 토큰을 Redis 블랙리스트에 TTL과 함께 저장하고, 해당 사용자의 리프레시 토큰을 삭제합니다. Authorization: Bearer {accessToken} 필요.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "토큰 없음 또는 만료", content = @Content)
    })
    ResponseEntity<Void> logout(jakarta.servlet.http.HttpServletRequest request);

    @Operation(summary = "이메일 인증 메일 재발송", description = "미인증 계정에 이메일 인증번호를 다시 보냅니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "인증 메일 발송",
                content = @Content(schema = @Schema(implementation = EmailVerificationResponse.class))),
        @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    ResponseEntity<EmailVerificationResponse> resendEmailVerification(
            EmailVerificationResendRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest);

    @Operation(summary = "이메일 인증 확인", description = "인증번호를 확인하고 사용자 이메일 인증 상태를 완료로 변경합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "이메일 인증 완료",
                content = @Content(schema = @Schema(implementation = EmailVerificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "인증번호 오류 또는 만료", content = @Content),
        @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    ResponseEntity<EmailVerificationResponse> confirmEmailVerification(
            EmailVerificationConfirmRequest request);
}
