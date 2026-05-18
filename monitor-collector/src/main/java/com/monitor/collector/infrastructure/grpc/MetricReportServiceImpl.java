package com.monitor.collector.infrastructure.grpc;

import com.monitor.collector.application.service.MetricIngestionService;
import com.monitor.collector.grpc.proto.*;
import com.monitor.collector.infrastructure.redis.ConnectionStateManager;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricReportServiceImpl extends AgentServiceGrpc.AgentServiceImplBase {

    private final MetricIngestionService ingestionService;
    private final ConnectionStateManager connectionManager;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        String hostname = request.getHostname();
        String ipAddress = request.getIpAddress();
        
        // 先查找是否已存在该节点（基于 hostname + ip）
        String nodeId = findExistingNode(hostname, ipAddress);
        
        if (nodeId != null) {
            log.info("Agent reconnected: hostname={}, ip={}, existing nodeId={}",
                    hostname, ipAddress, nodeId);
        } else {
            nodeId = UUID.randomUUID().toString();
            log.info("New agent registered: hostname={}, ip={}, assigned nodeId={}",
                    hostname, ipAddress, nodeId);
        }

        // 保存或更新节点信息到数据库（包括系统详细信息）
        try {
            String sql = "INSERT INTO host_nodes (id, hostname, ip_address, os_type, os_version, os_arch, " +
                    "cpu_cores, total_memory_bytes, total_disk_bytes, status, agent_version, " +
                    "network_interfaces, disk_info, last_heartbeat) " +
                    "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, 'ONLINE', ?, ?::text[], ?::jsonb, NOW()) " +
                    "ON CONFLICT (id) DO UPDATE SET " +
                    "hostname = EXCLUDED.hostname, " +
                    "ip_address = EXCLUDED.ip_address, " +
                    "os_type = EXCLUDED.os_type, " +
                    "os_version = EXCLUDED.os_version, " +
                    "os_arch = EXCLUDED.os_arch, " +
                    "cpu_cores = EXCLUDED.cpu_cores, " +
                    "total_memory_bytes = EXCLUDED.total_memory_bytes, " +
                    "total_disk_bytes = EXCLUDED.total_disk_bytes, " +
                    "agent_version = EXCLUDED.agent_version, " +
                    "network_interfaces = EXCLUDED.network_interfaces, " +
                    "disk_info = EXCLUDED.disk_info, " +
                    "status = 'ONLINE', " +
                    "last_heartbeat = NOW(), " +
                    "updated_at = NOW()";
            
            // 处理网络接口数组
            String[] networkInterfaces = request.getLabelsMap().getOrDefault("network_interfaces", "").split(",");
            
            // 处理磁盘信息 JSON
            String diskInfoJson = request.getLabelsMap().getOrDefault("disk_info", "[]");
            
            jdbcTemplate.update(sql, nodeId, hostname, ipAddress,
                    request.getOsType(), request.getOsVersion(), 
                    request.getLabelsMap().getOrDefault("os_arch", ""),
                    Integer.parseInt(request.getLabelsMap().getOrDefault("cpu_cores", "0")),
                    Long.parseLong(request.getLabelsMap().getOrDefault("total_memory_bytes", "0")),
                    Long.parseLong(request.getLabelsMap().getOrDefault("total_disk_bytes", "0")),
                    request.getAgentVersion(),
                    networkInterfaces,
                    diskInfoJson);
            
            log.info("Node saved/updated in database: nodeId={}, hostname={}", nodeId, hostname);
        } catch (Exception e) {
            log.error("Failed to save node to database: nodeId={}", nodeId, e);
        }

        // 根据 OS 类型自动分配告警模板
        List<String> assignedTemplates = assignTemplatesByOS(request.getOsType());
        
        // 保存节点-模板关联
        saveNodeTemplateRelations(nodeId, assignedTemplates);

        RegisterResponse response = RegisterResponse.newBuilder()
                .setNodeId(nodeId)
                .setCollectInterval(30)
                .addAllEnabledItems(java.util.List.of(
                        "system.cpu.util[,idle]", "system.cpu.util[,system]",
                        "system.mem.used.pct", "system.disk.used.pct[/]",
                        "system.net.if.in[eth0]", "system.net.if.out[eth0]",
                        "system.proc.num[]", "system.cpu.load[all,avg1]"
                ))
                .setServerTime(Instant.now().toString())
                .addAllAssignedTemplates(assignedTemplates)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    /**
     * 根据 hostname 和 IP 查找已存在的节点
     * 用于避免重复创建节点记录
     */
    private String findExistingNode(String hostname, String ipAddress) {
        try {
            // 优先使用 IP 地址匹配（同一台机器可能 hostname 会变）
            String sql = "SELECT id FROM host_nodes WHERE ip_address = ? AND status = 'ONLINE' " +
                        "ORDER BY last_heartbeat DESC LIMIT 1";
            List<String> results = jdbcTemplate.queryForList(sql, String.class, ipAddress);
            
            if (!results.isEmpty()) {
                return results.get(0);
            }
            
            // 如果 IP 没匹配，尝试 hostname 匹配
            sql = "SELECT id FROM host_nodes WHERE hostname = ? " +
                  "ORDER BY last_heartbeat DESC LIMIT 1";
            results = jdbcTemplate.queryForList(sql, String.class, hostname);
            
            if (!results.isEmpty()) {
                return results.get(0);
            }
        } catch (Exception e) {
            log.warn("Failed to find existing node: hostname={}, ip={}", hostname, ipAddress, e);
        }
        
        return null;
    }
    
    /**
     * 根据操作系统类型自动分配告警模板
     */
    private List<String> assignTemplatesByOS(String osType) {
        List<String> templates = new ArrayList<>();
        
        if (osType != null && osType.toLowerCase().contains("linux")) {
            // Linux 系统分配 Linux 监控模板
            templates.add("a1111111-1111-1111-1111-111111111111");
            log.info("Assigned Linux monitoring template to node");
        }
        // 未来可以添加 Windows、macOS 等其他模板
        
        return templates;
    }
    
    /**
     * 保存节点-模板关联关系
     */
    private void saveNodeTemplateRelations(String nodeId, List<String> templateIds) {
        if (templateIds.isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO node_alert_templates (node_id, template_id, enabled) " +
                     "VALUES (?::uuid, ?::uuid, TRUE) " +
                     "ON CONFLICT (node_id, template_id) DO NOTHING";
        
        for (String templateId : templateIds) {
            try {
                jdbcTemplate.update(sql, nodeId, templateId);
                log.info("Saved node-template relation: nodeId={}, templateId={}", nodeId, templateId);
            } catch (Exception e) {
                log.error("Failed to save node-template relation: nodeId={}, templateId={}", 
                         nodeId, templateId, e);
            }
        }
    }

    @Override
    public StreamObserver<MetricReport> reportMetrics(StreamObserver<MetricAck> responseObserver) {
        return new StreamObserver<MetricReport>() {
            @Override
            public void onNext(MetricReport report) {
                ingestionService.ingest(report);
                MetricAck ack = MetricAck.newBuilder()
                        .setReceivedCount(report.getMetricsCount())
                        .setServerTime(Instant.now().toString())
                        .build();
                responseObserver.onNext(ack);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error in metric stream", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Heartbeat> heartbeatStream(StreamObserver<HeartbeatAck> responseObserver) {
        return new StreamObserver<Heartbeat>() {
            @Override
            public void onNext(Heartbeat heartbeat) {
                connectionManager.updateHeartbeat(heartbeat);
                HeartbeatAck ack = HeartbeatAck.newBuilder()
                        .setServerTime(Instant.now().toString())
                        .build();
                responseObserver.onNext(ack);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error in heartbeat stream", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
