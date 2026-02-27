package com.example.coalawebbackend.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private final StringRedisTemplate redisTemplate;

    @Value("${spring.data.redis.host:unknown}")
    private String host;

    @Value("${spring.data.redis.port:0}")
    private int port;

    @PostConstruct
    public void logRedisConnection() {
        try {
            String ping = redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("Connected to Redis at {}:{} - PING response: {}", host, port, ping);
        } catch (Exception e) {
            log.error("Failed to connect to Redis at {}:{}", host, port, e);
        }
    }
}
