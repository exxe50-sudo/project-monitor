package com.monitor.server.domain.alert.rule;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "ref_type", length = 20)
    private String refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "trigger_conf", columnDefinition = "JSONB")
    private String triggerConf;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "notify_channels", columnDefinition = "JSONB")
    private String notifyChannels;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (enabled == null) enabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
