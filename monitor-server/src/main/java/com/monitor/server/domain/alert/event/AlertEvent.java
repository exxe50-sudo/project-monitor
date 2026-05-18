package com.monitor.server.domain.alert.event;

import com.monitor.common.enums.AlertEventType;
import com.monitor.common.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private AlertEventType eventType;

    @Column(name = "trigger_name", length = 200)
    private String triggerName;

    @Column(name = "trigger_expr", columnDefinition = "TEXT")
    private String triggerExpr;

    @Column(name = "current_value")
    private Double currentValue;

    @Column(name = "ref_type", length = 20)
    private String refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "ack_status", length = 20)
    private String ackStatus;

    @Column(name = "ack_user_id")
    private UUID ackUserId;

    @Column(name = "ack_time")
    private Instant ackTime;

    @Column(name = "ack_comment", columnDefinition = "TEXT")
    private String ackComment;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(columnDefinition = "JSONB")
    private String tags;

    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) triggeredAt = Instant.now();
    }
}
