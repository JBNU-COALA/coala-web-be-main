package com.example.coalawebbackend.api.auth.dto;

/**
 * JWT 발급용 토큰 쌍 (Controller/Facade에서 TokenResponse로 확장)
 */
public record TokenInfo(String accessToken, String refreshToken) {}
