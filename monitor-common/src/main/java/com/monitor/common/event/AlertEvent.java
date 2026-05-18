package com.monitor.common.event;

import com.monitor.common.enums.AlertEventType;
import com.monitor.common.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {
    private UUID eventId;
    private UUID ruleId;
    private UUID projectId;
    private Severity severity;
    private AlertEventType eventType;
    private String triggerName;
    private String message;
    private Double currentValue;
    private UUID refId;
    private String refType;
    private Instant timestamp;
}
