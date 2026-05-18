package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.application.service.ArchitectureService;
import com.monitor.server.domain.architecture.ArchitectureNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/architecture")
@RequiredArgsConstructor
public class ArchitectureController {
    private final ArchitectureService architectureService;

    @GetMapping("/tree")
    public ApiResponse<List<ArchitectureNode>> getTree(@PathVariable UUID projectId) {
        return ApiResponse.success(architectureService.getProjectTree(projectId));
    }

    @PostMapping("/nodes")
    public ApiResponse<ArchitectureNode> addNode(@PathVariable UUID projectId, @RequestBody ArchitectureNode node) {
        node.setProjectId(projectId);
        return ApiResponse.success(architectureService.addNode(node));
    }

    @PutMapping("/nodes/{nodeId}")
    public ApiResponse<ArchitectureNode> updateNode(@PathVariable UUID projectId, @PathVariable UUID nodeId, @RequestBody ArchitectureNode node) {
        return ApiResponse.success(architectureService.updateNode(nodeId, node));
    }

    @DeleteMapping("/nodes/{nodeId}")
    public ApiResponse<Void> deleteNode(@PathVariable UUID projectId, @PathVariable UUID nodeId) {
        architectureService.deleteNode(nodeId);
        return ApiResponse.success(null);
    }
}
