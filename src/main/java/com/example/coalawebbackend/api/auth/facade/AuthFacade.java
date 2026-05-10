package com.example.coalawebbackend.api.auth.facade;

import com.example.coalawebbackend.api.auth.dto.LoginRequest;
import com.example.coalawebbackend.api.auth.dto.TokenInfo;
import com.example.coalawebbackend.api.auth.dto.TokenRefreshRequest;
import com.example.coalawebbackend.api.auth.dto.TokenResponse;
import com.example.coalawebbackend.api.auth.exception.AuthException;
import com.example.coalawebbackend.api.auth.service.EmailVerificationService;
import com.example.coalawebbackend.api.user.dto.UserResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.jwt.JwtTokenProvider;
import com.example.coalawebbackend.common.jwt.LogoutTokenStore;
import com.example.coalawebbackend.common.jwt.RefreshTokenStore;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthFacade {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStore refreshTokenStore;
    private final LogoutTokenStore logoutTokenStore;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new AuthException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.isVerified()) {
            emailVerificationService.issue(user);
            throw new AuthException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        TokenInfo tokens =
                jwtTokenProvider.generateTokenInfo(
                        String.valueOf(user.getId()), tokenClaims(user));
        refreshTokenStore.save(String.valueOf(user.getId()), tokens.refreshToken());
        return new TokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                TOKEN_TYPE,
                UserResponse.from(user));
    }

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        String subject;
        try {
            subject = jwtTokenProvider.getSubject(request.refreshToken());
        } catch (Exception e) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (jwtTokenProvider.isExpired(request.refreshToken())) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = Long.parseLong(subject);
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        if (!refreshTokenStore.validate(String.valueOf(userId), request.refreshToken())) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        TokenInfo tokens =
                jwtTokenProvider.generateTokenInfo(
                        String.valueOf(user.getId()), tokenClaims(user));
        refreshTokenStore.save(String.valueOf(user.getId()), tokens.refreshToken());
        return new TokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                TOKEN_TYPE,
                UserResponse.from(user));
    }

    @Transactional
    public void logout(String accessToken) {
        String userId;
        try {
            userId = jwtTokenProvider.getSubject(accessToken);
        } catch (JwtException | IllegalArgumentException e) {
            return;
        }

        long ttlMillis = jwtTokenProvider.getRemainingExpirationMillis(accessToken);
        logoutTokenStore.add(accessToken, ttlMillis);
        refreshTokenStore.delete(userId);
    }

    private Map<String, Object> tokenClaims(User user) {
        return Map.of(
                "email", user.getEmail(),
                "role", user.getRole().authority());
    }
}
