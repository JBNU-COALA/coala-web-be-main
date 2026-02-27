package com.example.coalawebbackend.common.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 로그아웃 시 액세스 토큰을 Redis에 TTL과 함께 저장(블랙리스트).
 * TTL 동안 해당 토큰으로는 인증 불가.
 */
@Component
@RequiredArgsConstructor
public class LogoutTokenStore {

    private static final String KEY_PREFIX = "BL:";

    private final StringRedisTemplate redisTemplate;

    /**
     * 로그아웃한 액세스 토큰을 Redis에 저장. TTL(ms) 동안 유효.
     */
    public void add(String accessToken, long ttlMillis) {
        if (ttlMillis <= 0) {
            return;
        }
        String key = KEY_PREFIX + hash(accessToken);
        Duration ttl = Duration.ofMillis(ttlMillis);
        redisTemplate.opsForValue().set(key, "1", ttl);
    }

    /**
     * 해당 액세스 토큰이 로그아웃(블랙리스트) 처리되었는지 여부.
     */
    public boolean isBlacklisted(String accessToken) {
        String key = KEY_PREFIX + hash(accessToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
