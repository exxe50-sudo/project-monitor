package com.monitor.server.infrastructure.websocket;

import com.monitor.common.event.DashboardSnapshot;
import com.monitor.common.event.NodeStatusEvent;
import com.monitor.common.event.WsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardPushService {
    private final SimpMessagingTemplate messagingTemplate;

    public void pushDashboardSnapshot(UUID projectId, DashboardSnapshot snapshot) {
        String destination = "/topic/project/" + projectId + "/dashboard";
        WsMessage msg = WsMessage.builder()
                .type("DASHBOARD_SNAPSHOT")
                .payload(snapshot)
                .timestamp(Instant.now())
                .build();
        messagingTemplate.convertAndSend(destination, msg);
        log.debug("Pushed dashboard snapshot to {}", destination);
    }

    public void pushNodeStatusChange(UUID projectId, NodeStatusEvent event) {
        String destination = "/topic/project/" + projectId + "/status";
        WsMessage msg = WsMessage.builder()
                .type("NODE_STATUS_CHANGE")
                .payload(event)
                .timestamp(Instant.now())
                .build();
        messagingTemplate.convertAndSend(destination, msg);
        log.info("Pushed node status change: {} -> {} for node {}", event.getOldStatus(), event.getNewStatus(), event.getHostNodeId());
    }

    public void pushAlert(UUID projectId, Object alertData, String type) {
        String destination = "/topic/project/" + projectId + "/alerts";
        WsMessage msg = WsMessage.builder()
                .type(type)
                .payload(alertData)
                .timestamp(Instant.now())
                .build();
        messagingTemplate.convertAndSend(destination, msg);
    }

    public void pushToUser(String username, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }

    public void pushLiveMetric(UUID nodeId, String itemKey, double value) {
        String destination = "/topic/node/" + nodeId + "/metrics/live";
        WsMessage msg = WsMessage.builder()
                .type("LIVE_METRIC")
                .payload(new LiveMetricData(nodeId, itemKey, value))
                .timestamp(Instant.now())
                .build();
        messagingTemplate.convertAndSend(destination, msg);
    }

    public record LiveMetricData(UUID nodeId, String itemKey, double value) {}
}
