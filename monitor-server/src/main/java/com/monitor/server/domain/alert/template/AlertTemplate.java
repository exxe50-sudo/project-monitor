package com.monitor.server.domain.alert.template;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alert_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_builtin", nullable = false)
    private Boolean isBuiltin;

    @Column(name = "apply_scope", length = 50)
    private String applyScope;

    @Column
    private Integer version;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "templateId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TemplateItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "templateId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TemplateTrigger> triggers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (isBuiltin == null) isBuiltin = false;
        if (enabled == null) enabled = true;
        if (version == null) version = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
