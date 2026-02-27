package com.example.coalawebbackend.common.jwt;

import com.example.coalawebbackend.api.auth.dto.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = createSecretKey(secret);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    private static SecretKey createSecretKey(String secret) {
        try {
            byte[] keyBytes =
                    MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("SHA-256 not available", e);
        }
    }

    /**
     * Access Token / Refresh Token 동시 발급
     */
    public TokenInfo generateTokenInfo(String subject, Map<String, Object> claims) {
        String accessToken = createToken(subject, claims, accessTokenExpiration);
        String refreshToken = createToken(subject, claims, refreshTokenExpiration);
        return new TokenInfo(accessToken, refreshToken);
    }

    /**
     * 단일 JWT 생성
     */
    public String createToken(String subject, Map<String, Object> claims, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        var builder =
                Jwts.builder()
                        .setSubject(subject)
                        .setIssuedAt(now)
                        .setExpiration(expiry);

        if (claims != null) {
            claims.forEach(builder::claim);
        }

        return builder.signWith(secretKey).compact();
    }

    /**
     * 토큰 파싱 + 서명/만료 검증
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰 만료 여부만 단순 체크
     */
    public boolean isExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiresAt = claims.getExpiration();
            return expiresAt != null && expiresAt.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * subject(username 등) 가져오기
     */
    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * String 타입 클레임 파싱
     */
    public String getClaimAsString(String token, String name) {
        return parseToken(token).get(name, String.class);
    }
}
