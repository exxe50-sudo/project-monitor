package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.application.service.ProjectService;
import com.monitor.server.domain.project.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<List<Project>> list() {
        return ApiResponse.success(projectService.listProjects());
    }

    @GetMapping("/{id}")
    public ApiResponse<Project> get(@PathVariable UUID id) {
        return ApiResponse.success(projectService.getProject(id));
    }

    @PostMapping
    public ApiResponse<Project> create(@RequestBody Project project) {
        return ApiResponse.success(projectService.createProject(project));
    }

    @PutMapping("/{id}")
    public ApiResponse<Project> update(@PathVariable UUID id, @RequestBody Project project) {
        return ApiResponse.success(projectService.updateProject(id, project));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ApiResponse.success(null);
    }
}
