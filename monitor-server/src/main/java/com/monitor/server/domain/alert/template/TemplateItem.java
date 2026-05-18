package com.monitor.server.domain.alert.template;

import com.monitor.common.enums.MetricType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_template_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "item_key", nullable = false, length = 200)
    private String itemKey;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", length = 30)
    private MetricType metricType;

    @Column(name = "collect_interval")
    private Integer collectInterval;

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
