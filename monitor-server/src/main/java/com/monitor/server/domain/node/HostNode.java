package com.monitor.server.domain.node;

import com.monitor.common.enums.NodeStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "host_nodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String hostname;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "os_type", length = 50)
    private String osType;

    @Column(name = "os_version", length = 100)
    private String osVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NodeStatus status;

    @Column(name = "agent_version", length = 20)
    private String agentVersion;

    @Column(name = "labels", columnDefinition = "JSONB")
    private String labels;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Column(name = "ssh_config", columnDefinition = "JSONB")
    private String sshConfig;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        status = NodeStatus.OFFLINE;
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
