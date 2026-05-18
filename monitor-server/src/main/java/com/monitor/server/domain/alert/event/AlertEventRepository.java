package com.monitor.server.domain.alert.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AlertEventRepository extends JpaRepository<AlertEvent, UUID> {
    @Query("SELECT e FROM AlertEvent e WHERE e.eventType = 'PROBLEM' ORDER BY e.triggeredAt DESC")
    List<AlertEvent> findActiveAlerts();

    @Query("SELECT e FROM AlertEvent e WHERE e.ruleId = :ruleId AND e.refId = :refId AND e.eventType = 'PROBLEM'")
    AlertEvent findActiveByRuleAndRef(@Param("ruleId") UUID ruleId, @Param("refId") UUID refId);

    @Query("SELECT COUNT(e) FROM AlertEvent e WHERE e.eventType = 'PROBLEM' AND e.triggeredAt > :since")
    long countActiveAlertsSince(@Param("since") Instant since);

    List<AlertEvent> findByRefIdOrderByTriggeredAtDesc(UUID refId);
}
