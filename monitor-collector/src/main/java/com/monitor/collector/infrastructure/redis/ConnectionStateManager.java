package com.monitor.collector.infrastructure.redis;

import com.monitor.collector.grpc.proto.Heartbeat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionStateManager {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "node:status:";
    private static final Duration TTL = Duration.ofSeconds(15);

    public void updateHeartbeat(Heartbeat heartbeat) {
        String key = KEY_PREFIX + heartbeat.getNodeId();
        String status = heartbeat.getInfo().getAgentStatus();
        redisTemplate.opsForValue().set(key, status, TTL);
        log.debug("Heartbeat updated for node {}: status={}", heartbeat.getNodeId(), status);
    }

    public String getNodeStatus(String nodeId) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + nodeId);
    }

    public boolean isNodeOnline(String nodeId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + nodeId));
    }
}
