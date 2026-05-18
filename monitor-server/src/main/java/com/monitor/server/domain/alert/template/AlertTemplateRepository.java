package com.monitor.server.domain.alert.template;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface AlertTemplateRepository extends JpaRepository<AlertTemplate, UUID> {
    @Query("SELECT t FROM AlertTemplate t LEFT JOIN FETCH t.items LEFT JOIN FETCH t.triggers WHERE t.id = :id")
    AlertTemplate findByIdWithDetails(UUID id);

    List<AlertTemplate> findByApplyScope(String applyScope);

    List<AlertTemplate> findByIsBuiltinTrue();
}
