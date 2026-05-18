package com.monitor.server.domain.notification;

import com.monitor.common.enums.NotifyChannelType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notify_channels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private NotifyChannelType channelType;

    @Column(columnDefinition = "JSONB")
    private String config;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (enabled == null) enabled = true;
    }
}
