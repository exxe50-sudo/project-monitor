package com.monitor.server.domain.service;

import com.monitor.common.enums.ServiceStatus;
import com.monitor.common.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitored_services")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredService {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(name = "host_node_id")
    private UUID hostNodeId;

    @Column
    private Integer port;

    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceStatus status;

    @Column(length = 50)
    private String version;

    @Column(columnDefinition = "JSONB")
    private String labels;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        status = ServiceStatus.STOPPED;
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
