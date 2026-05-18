package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.application.service.AlertTemplateService;
import com.monitor.server.domain.alert.template.AlertTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class AlertTemplateController {
    private final AlertTemplateService templateService;

    @GetMapping
    public ApiResponse<List<AlertTemplate>> list() {
        return ApiResponse.success(templateService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertTemplate> get(@PathVariable UUID id) {
        return ApiResponse.success(templateService.getWithDetails(id));
    }

    @PostMapping
    public ApiResponse<AlertTemplate> create(@RequestBody AlertTemplate template) {
        return ApiResponse.success(templateService.create(template));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        templateService.delete(id);
        return ApiResponse.success(null);
    }
}
