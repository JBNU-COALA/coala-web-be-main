package com.example.coalawebbackend.common.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "RT:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private String key(String userId) {
        return KEY_PREFIX + userId;
    }

    public void save(String userId, String refreshToken) {
        String hashed = hash(refreshToken);
        Duration ttl = Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMillis());
        redisTemplate.opsForValue().set(key(userId), hashed, ttl);
    }

    public boolean validate(String userId, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(key(userId));
        if (stored == null) {
            return false;
        }
        String hashed = hash(refreshToken);
        return stored.equals(hashed);
    }

    public void delete(String userId) {
        redisTemplate.delete(key(userId));
    }

    private String hash(String value) {
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
