package com.example.coalawebbackend.api.auth.controller;

import com.example.coalawebbackend.api.auth.dto.EmailVerificationConfirmRequest;
import com.example.coalawebbackend.api.auth.dto.EmailVerificationResendRequest;
import com.example.coalawebbackend.api.auth.dto.EmailVerificationResponse;
import com.example.coalawebbackend.api.auth.dto.LoginRequest;
import com.example.coalawebbackend.api.auth.dto.TokenRefreshRequest;
import com.example.coalawebbackend.api.auth.dto.TokenResponse;
import com.example.coalawebbackend.api.auth.facade.AuthFacade;
import com.example.coalawebbackend.api.auth.service.EmailVerificationService;
import com.example.coalawebbackend.common.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerSpec {

    private final AuthFacade authFacade;
    private final EmailVerificationService emailVerificationService;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    @Override
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        rateLimitService.check(httpRequest, "auth:login", 5, Duration.ofMinutes(1));
        TokenResponse response = authFacade.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Override
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse response = authFacade.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            authFacade.logout(token);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-verification/resend")
    @Override
    public ResponseEntity<EmailVerificationResponse> resendEmailVerification(
            @Valid @RequestBody EmailVerificationResendRequest request,
            HttpServletRequest httpRequest) {
        rateLimitService.check(httpRequest, "auth:email-resend", 3, Duration.ofMinutes(5));
        return ResponseEntity.ok(emailVerificationService.resend(request.email()));
    }

    @PostMapping("/email-verification/confirm")
    @Override
    public ResponseEntity<EmailVerificationResponse> confirmEmailVerification(
            @Valid @RequestBody EmailVerificationConfirmRequest request) {
        return ResponseEntity.ok(emailVerificationService.confirm(request.email(), request.code()));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
