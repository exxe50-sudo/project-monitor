package com.monitor.server.domain.alert.template;

import com.monitor.common.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_template_triggers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTrigger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expression;

    @Column(name = "recover_expr", columnDefinition = "TEXT")
    private String recoverExpr;

    @Column(name = "trigger_desc", columnDefinition = "TEXT")
    private String triggerDesc;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (enabled == null) enabled = true;
    }
}
