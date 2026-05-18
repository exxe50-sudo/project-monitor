package com.monitor.server.domain.architecture;

import com.monitor.common.enums.ArchitectureNodeType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "architecture_nodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchitectureNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 20)
    private ArchitectureNodeType nodeType;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "ref_type", length = 20)
    private String refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "tree_path", length = 500)
    private String treePath;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "style_config", columnDefinition = "JSONB")
    private String styleConfig;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    private List<ArchitectureNode> children = new ArrayList<>();

    @Transient
    private String status;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
