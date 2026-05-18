package com.monitor.server.domain.architecture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ArchitectureNodeRepository extends JpaRepository<ArchitectureNode, UUID> {
    List<ArchitectureNode> findByProjectIdOrderBySortOrder(UUID projectId);

    List<ArchitectureNode> findByParentIdOrderBySortOrder(UUID parentId);

    List<ArchitectureNode> findByProjectIdAndParentIdIsNullOrderBySortOrder(UUID projectId);

    @Query("SELECT a FROM ArchitectureNode a WHERE a.refId = :refId")
    List<ArchitectureNode> findByRefId(@Param("refId") UUID refId);

    @Modifying
    @Query("DELETE FROM ArchitectureNode a WHERE a.parentId = :parentId")
    void deleteByParentId(@Param("parentId") UUID parentId);
}
