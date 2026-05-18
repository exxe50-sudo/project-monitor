package com.monitor.server.application.service;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.domain.project.Project;
import com.monitor.server.domain.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<Project> listProjects() {
        return projectRepository.findAllActive();
    }

    public Project getProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
    }

    @Transactional
    public Project createProject(Project project) {
        project.setSortOrder((int) (projectRepository.count() + 1));
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(UUID id, Project updated) {
        Project project = getProject(id);
        project.setName(updated.getName());
        project.setDescription(updated.getDescription());
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = getProject(id);
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }
}
