package com.monitor.common.event;

import com.monitor.common.enums.NodeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeStatusEvent {
    private UUID nodeId;
    private UUID hostNodeId;
    private NodeStatus oldStatus;
    private NodeStatus newStatus;
    private List<String> affectedArchNodes;
    private Instant timestamp;
}
