package com.monitor.alerter.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SuppressionCache {
    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "suppress:";
    private static final Duration SUPPRESS_DURATION = Duration.ofMinutes(5);

    public boolean isSuppressed(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + key));
    }

    public void suppress(String key) {
        redisTemplate.opsForValue().set(PREFIX + key, "1", SUPPRESS_DURATION);
    }

    public void release(String key) {
        redisTemplate.delete(PREFIX + key);
    }
}
