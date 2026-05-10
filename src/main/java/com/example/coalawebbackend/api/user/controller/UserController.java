package com.example.coalawebbackend.api.user.controller;

import com.example.coalawebbackend.api.auth.dto.EmailVerificationResponse;
import com.example.coalawebbackend.api.user.dto.UserCreateRequest;
import com.example.coalawebbackend.api.user.facade.UserFacade;
import com.example.coalawebbackend.common.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/signup")
@RequiredArgsConstructor
public class UserController implements UserControllerSpec {

    private final UserFacade userFacade;
    private final RateLimitService rateLimitService;

    @PostMapping
    @Override
    public ResponseEntity<EmailVerificationResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        rateLimitService.check(httpRequest, "auth:signup", 3, Duration.ofMinutes(10));
        EmailVerificationResponse response = userFacade.createUser(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
