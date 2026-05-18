package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 节点告警模板管理接口
 */
@RestController
@RequestMapping("/api/v1/nodes/{nodeId}/templates")
@RequiredArgsConstructor
public class NodeTemplateController {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 获取节点已分配的告警模板列表
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getNodeTemplates(@PathVariable UUID nodeId) {
        String sql = "SELECT t.id, t.name, t.description, t.is_builtin, t.apply_scope, " +
                     "nat.enabled, nat.config, nat.created_at " +
                     "FROM node_alert_templates nat " +
                     "JOIN alert_templates t ON nat.template_id = t.id " +
                     "WHERE nat.node_id = ?::uuid " +
                     "ORDER BY t.name";
        
        List<Map<String, Object>> templates = jdbcTemplate.queryForList(sql, nodeId.toString());
        return ApiResponse.success(templates);
    }
    
    /**
     * 为节点分配告警模板
     */
    @PostMapping
    public ApiResponse<Void> assignTemplate(
            @PathVariable UUID nodeId,
            @RequestBody Map<String, Object> request) {
        
        String templateId = (String) request.get("templateId");
        Boolean enabled = (Boolean) request.getOrDefault("enabled", true);
        Map<String, Object> config = (Map<String, Object>) request.getOrDefault("config", null);
        
        String sql = "INSERT INTO node_alert_templates (node_id, template_id, enabled, config) " +
                     "VALUES (?::uuid, ?::uuid, ?, ?::jsonb) " +
                     "ON CONFLICT (node_id, template_id) DO UPDATE SET " +
                     "enabled = EXCLUDED.enabled, " +
                     "config = EXCLUDED.config, " +
                     "updated_at = NOW()";
        
        jdbcTemplate.update(sql, nodeId.toString(), templateId, enabled, 
                config != null ? config.toString() : null);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 移除节点的告警模板
     */
    @DeleteMapping("/{templateId}")
    public ApiResponse<Void> removeTemplate(
            @PathVariable UUID nodeId,
            @PathVariable UUID templateId) {
        
        String sql = "DELETE FROM node_alert_templates WHERE node_id = ?::uuid AND template_id = ?::uuid";
        jdbcTemplate.update(sql, nodeId.toString(), templateId.toString());
        
        return ApiResponse.success(null);
    }
    
    /**
     * 获取可分配的告警模板列表（所有内置模板）
     */
    @GetMapping("/available")
    public ApiResponse<List<Map<String, Object>>> getAvailableTemplates() {
        String sql = "SELECT id, name, description, is_builtin, apply_scope, version, enabled " +
                     "FROM alert_templates " +
                     "WHERE is_builtin = TRUE AND enabled = TRUE " +
                     "ORDER BY name";
        
        List<Map<String, Object>> templates = jdbcTemplate.queryForList(sql);
        return ApiResponse.success(templates);
    }
}
