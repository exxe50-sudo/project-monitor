package com.monitor.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSnapshot {
    private UUID projectId;
    private int onlineCount;
    private int totalCount;
    private int alertCount;
    private Map<String, Double> topMetrics;
    private Instant timestamp;
}
