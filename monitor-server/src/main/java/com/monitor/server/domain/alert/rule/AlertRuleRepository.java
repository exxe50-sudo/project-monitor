package com.monitor.server.domain.alert.rule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {
    List<AlertRule> findByProjectId(UUID projectId);

    List<AlertRule> findByRefTypeAndRefId(String refType, UUID refId);

    List<AlertRule> findByEnabledTrue();

    @Query("SELECT r FROM AlertRule r WHERE r.enabled = true AND r.projectId = :projectId")
    List<AlertRule> findEnabledByProjectId(@Param("projectId") UUID projectId);
}
