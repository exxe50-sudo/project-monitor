package com.monitor.server.domain.node;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HostNodeRepository extends JpaRepository<HostNode, UUID> {
    Optional<HostNode> findByHostname(String hostname);

    @Query("SELECT h FROM HostNode h WHERE h.lastHeartbeat < :threshold AND h.status = 'ONLINE'")
    List<HostNode> findTimeoutNodes(@Param("threshold") Instant threshold);
}
