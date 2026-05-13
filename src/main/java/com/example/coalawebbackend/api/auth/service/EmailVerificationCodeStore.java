package com.example.coalawebbackend.api.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationCodeStore {

    private static final String KEY_PREFIX = "EV:";
    private static final String PASSWORD_RESET_KEY_PREFIX = "PR:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.email-verification.ttl-minutes:10}")
    private long ttlMinutes;

    public void save(String email, String code) {
        redisTemplate
                .opsForValue()
                .set(key(KEY_PREFIX, email), hash(code), Duration.ofMinutes(ttlMinutes));
    }

    public boolean validate(String email, String code) {
        String stored = redisTemplate.opsForValue().get(key(KEY_PREFIX, email));
        if (stored == null) {
            return false;
        }
        return MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8),
                hash(code).getBytes(StandardCharsets.UTF_8));
    }

    public void delete(String email) {
        redisTemplate.delete(key(KEY_PREFIX, email));
    }

    public void savePasswordReset(String email, String code) {
        redisTemplate
                .opsForValue()
                .set(key(PASSWORD_RESET_KEY_PREFIX, email), hash(code), Duration.ofMinutes(ttlMinutes));
    }

    public boolean validatePasswordReset(String email, String code) {
        String stored = redisTemplate.opsForValue().get(key(PASSWORD_RESET_KEY_PREFIX, email));
        if (stored == null) {
            return false;
        }
        return MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8),
                hash(code).getBytes(StandardCharsets.UTF_8));
    }

    public void deletePasswordReset(String email) {
        redisTemplate.delete(key(PASSWORD_RESET_KEY_PREFIX, email));
    }

    private String key(String prefix, String email) {
        return prefix + email.trim().toLowerCase();
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
