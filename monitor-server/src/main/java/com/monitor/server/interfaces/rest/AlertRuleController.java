package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.domain.alert.rule.AlertRule;
import com.monitor.server.domain.alert.rule.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class AlertRuleController {
    private final AlertRuleRepository ruleRepository;

    @GetMapping
    public ApiResponse<List<AlertRule>> list(@RequestParam(required = false) UUID projectId) {
        if (projectId != null) {
            return ApiResponse.success(ruleRepository.findByProjectId(projectId));
        }
        return ApiResponse.success(ruleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertRule> get(@PathVariable UUID id) {
        return ApiResponse.success(ruleRepository.findById(id).orElse(null));
    }

    @PostMapping
    public ApiResponse<AlertRule> create(@RequestBody AlertRule rule) {
        return ApiResponse.success(ruleRepository.save(rule));
    }

    @PutMapping("/{id}")
    public ApiResponse<AlertRule> update(@PathVariable UUID id, @RequestBody AlertRule rule) {
        rule.setId(id);
        return ApiResponse.success(ruleRepository.save(rule));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        ruleRepository.deleteById(id);
        return ApiResponse.success(null);
    }
}
