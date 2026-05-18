package com.monitor.server.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotifyChannelRepository extends JpaRepository<NotifyChannel, UUID> {
    List<NotifyChannel> findByEnabledTrue();
}
