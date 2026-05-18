package com.monitor.agent.collector;

import com.monitor.agent.collector.impl.*;
import com.monitor.agent.grpc.proto.*;
import com.monitor.agent.transport.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 采集器管理器 - 负责管理和调度所有采集器
 */
public class CollectorManager {
    private static final Logger log = LoggerFactory.getLogger(CollectorManager.class);
    
    private final GrpcClient grpcClient;
    private final String nodeId;
    private final List<Collector> collectors;
    
    public CollectorManager(GrpcClient grpcClient, String nodeId) {
        this.grpcClient = grpcClient;
        this.nodeId = nodeId;
        
        // 初始化所有采集器（类似 Zabbix Agent 的内置采集项）
        this.collectors = Arrays.asList(
                new CpuCollector(),
                new MemoryCollector(),
                new DiskCollector(),
                new NetworkCollector(),
                new ProcessCollector(),
                new PortCollector()
        );
        
        log.info("Initialized {} collectors", collectors.size());
    }
    
    /**
     * 执行采集并发送指标
     */
    public void collectAndSend() {
        try {
            List<Metric> allMetrics = new ArrayList<>();
            
            for (Collector collector : collectors) {
                try {
                    List<Metric> metrics = collector.collect();
                    allMetrics.addAll(metrics);
                    log.debug("Collector {} collected {} metrics", collector.name(), metrics.size());
                } catch (Exception e) {
                    log.warn("Collector {} failed: {}", collector.name(), e.getMessage());
                }
            }
            
            if (!allMetrics.isEmpty()) {
                MetricReport report = MetricReport.newBuilder()
                        .setAgentId(System.getenv("AGENT_AGENT_ID"))
                        .setNodeId(nodeId)
                        .setTimestamp(System.currentTimeMillis())
                        .addAllMetrics(allMetrics)
                        .build();
                
                grpcClient.sendMetric(report);
                log.info("Sent {} metrics to collector", allMetrics.size());
            }
        } catch (Exception e) {
            log.error("Error in collectAndSend", e);
        }
    }
    
    /**
     * 发送心跳
     */
    public void sendHeartbeat() {
        try {
            // 获取 CPU、内存、磁盘使用率用于心跳
            double cpuUtil = getCpuUtilization();
            double memUsedPct = getMemoryUsedPercent();
            double diskUsedPct = getDiskUsedPercent();
            
            HeartbeatInfo info = HeartbeatInfo.newBuilder()
                    .setCpuUtil(cpuUtil)
                    .setMemUsedPct(memUsedPct)
                    .setDiskUsedPct(diskUsedPct)
                    .setAgentStatus("active")
                    .build();
            
            Heartbeat heartbeat = Heartbeat.newBuilder()
                    .setAgentId(System.getenv("AGENT_AGENT_ID"))
                    .setTimestamp(System.currentTimeMillis())
                    .setNodeId(nodeId)
                    .setInfo(info)
                    .build();
            
            grpcClient.sendHeartbeat(heartbeat);
            log.debug("Heartbeat sent: cpu={:.2f}%, mem={:.2f}%, disk={:.2f}%",
                    cpuUtil, memUsedPct, diskUsedPct);
        } catch (Exception e) {
            log.error("Error in sendHeartbeat", e);
        }
    }
    
    private double getCpuUtilization() {
        try {
            CpuCollector cpuCollector = (CpuCollector) collectors.stream()
                    .filter(c -> c instanceof CpuCollector)
                    .findFirst()
                    .orElse(null);
            if (cpuCollector != null) {
                List<Metric> metrics = cpuCollector.collect();
                return metrics.stream()
                        .filter(m -> m.getItemKey().contains("user"))
                        .mapToDouble(Metric::getValue)
                        .sum();
            }
        } catch (Exception e) {
            log.debug("Failed to get CPU utilization", e);
        }
        return 0.0;
    }
    
    private double getMemoryUsedPercent() {
        try {
            MemoryCollector memCollector = (MemoryCollector) collectors.stream()
                    .filter(c -> c instanceof MemoryCollector)
                    .findFirst()
                    .orElse(null);
            if (memCollector != null) {
                List<Metric> metrics = memCollector.collect();
                return metrics.stream()
                        .filter(m -> m.getItemKey().contains("used_percent"))
                        .mapToDouble(Metric::getValue)
                        .findFirst()
                        .orElse(0.0);
            }
        } catch (Exception e) {
            log.debug("Failed to get memory used percent", e);
        }
        return 0.0;
    }
    
    private double getDiskUsedPercent() {
        try {
            DiskCollector diskCollector = (DiskCollector) collectors.stream()
                    .filter(c -> c instanceof DiskCollector)
                    .findFirst()
                    .orElse(null);
            if (diskCollector != null) {
                List<Metric> metrics = diskCollector.collect();
                return metrics.stream()
                        .filter(m -> m.getItemKey().contains("used_percent"))
                        .mapToDouble(Metric::getValue)
                        .findFirst()
                        .orElse(0.0);
            }
        } catch (Exception e) {
            log.debug("Failed to get disk used percent", e);
        }
        return 0.0;
    }
}
