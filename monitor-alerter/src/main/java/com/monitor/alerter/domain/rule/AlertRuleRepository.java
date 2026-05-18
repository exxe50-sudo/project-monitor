package com.monitor.alerter.domain.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AlertRuleRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<AlertRule> findEnabledRules(String nodeId, String itemKey) {
        String sql = """
            SELECT r.id, r.name, r.project_id, r.ref_type, r.ref_id, r.trigger_conf, r.notify_channels
            FROM alert_rules r
            JOIN alert_template_items ti ON ti.template_id = r.template_id AND ti.item_key = ?
            WHERE r.enabled = true
            AND (r.ref_id IS NULL OR r.ref_id = ?::uuid)
            """;
        return jdbcTemplate.query(sql, this::mapRule, itemKey, nodeId);
    }

    private AlertRule mapRule(ResultSet rs, int rowNum) throws SQLException {
        var rule = new AlertRule();
        rule.setId(UUID.fromString(rs.getString("id")));
        rule.setName(rs.getString("name"));
        rule.setProjectId(rs.getString("project_id") != null ? UUID.fromString(rs.getString("project_id")) : null);
        rule.setRefType(rs.getString("ref_type"));
        rule.setRefId(rs.getString("ref_id") != null ? UUID.fromString(rs.getString("ref_id")) : null);
        rule.setTriggerConfJson(rs.getString("trigger_conf"));
        rule.setNotifyChannelsJson(rs.getString("notify_channels"));
        return rule;
    }
}
