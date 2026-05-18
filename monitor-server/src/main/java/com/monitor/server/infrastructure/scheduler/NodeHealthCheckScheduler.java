package com.monitor.server.infrastructure.scheduler;

import com.monitor.common.enums.NodeStatus;
import com.monitor.common.event.NodeStatusEvent;
import com.monitor.server.domain.architecture.ArchitectureNode;
import com.monitor.server.domain.architecture.ArchitectureNodeRepository;
import com.monitor.server.domain.node.HostNode;
import com.monitor.server.domain.node.HostNodeRepository;
import com.monitor.server.infrastructure.websocket.DashboardPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeHealthCheckScheduler {
    private final HostNodeRepository hostNodeRepository;
    private final ArchitectureNodeRepository archNodeRepository;
    private final DashboardPushService pushService;

    @Scheduled(fixedRate = 10000) // 每10秒检查一次
    @Transactional
    public void checkNodeHealth() {
        Instant threshold = Instant.now().minus(15, ChronoUnit.SECONDS);
        List<HostNode> timeoutNodes = hostNodeRepository.findTimeoutNodes(threshold);

        for (HostNode node : timeoutNodes) {
            NodeStatus oldStatus = node.getStatus();
            node.setStatus(NodeStatus.OFFLINE);
            hostNodeRepository.save(node);
            log.warn("Node {} ({}) timed out, status changed to OFFLINE", node.getHostname(), node.getId());

            // 查找引用了此节点的架构节点，推送状态变更
            List<ArchitectureNode> archNodes = archNodeRepository.findByRefId(node.getId());
            Map<UUID, List<ArchitectureNode>> projectGroups = archNodes.stream()
                    .collect(Collectors.groupingBy(ArchitectureNode::getProjectId));

            for (Map.Entry<UUID, List<ArchitectureNode>> entry : projectGroups.entrySet()) {
                NodeStatusEvent event = NodeStatusEvent.builder()
                        .hostNodeId(node.getId())
                        .oldStatus(oldStatus)
                        .newStatus(NodeStatus.OFFLINE)
                        .affectedArchNodes(entry.getValue().stream().map(a -> a.getId().toString()).collect(Collectors.toList()))
                        .timestamp(Instant.now())
                        .build();
                pushService.pushNodeStatusChange(entry.getKey(), event);
            }
        }
    }
}
