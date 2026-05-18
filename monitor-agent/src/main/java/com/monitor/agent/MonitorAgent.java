package com.monitor.agent;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.collector.CollectorManager;
import com.monitor.agent.transport.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitor Agent - 轻量级系统监控代理
 * 参考 Zabbix Agent 设计：独立进程、轻量级、配置简单
 */
public class MonitorAgent {
    private static final Logger log = LoggerFactory.getLogger(MonitorAgent.class);
    
    private final String collectorHost;
    private final int collectorPort;
    private final String agentId;
    private final int heartbeatInterval;
    
    private GrpcClient grpcClient;
    private CollectorManager collectorManager;
    private ScheduledExecutorService scheduler;
    private volatile boolean running = false;
    
    public MonitorAgent() {
        // 从环境变量读取配置，支持 Docker 和 K8s 部署
        this.collectorHost = System.getenv("AGENT_COLLECTOR_HOST");
        this.collectorPort = parseIntEnv("AGENT_COLLECTOR_PORT", 9090);
        String tempAgentId = System.getenv("AGENT_AGENT_ID");
        this.heartbeatInterval = parseIntEnv("AGENT_HEARTBEAT_INTERVAL", 5);
        
        if (collectorHost == null || collectorHost.isEmpty()) {
            throw new IllegalArgumentException("AGENT_COLLECTOR_HOST environment variable is required");
        }
        
        if (tempAgentId == null || tempAgentId.isEmpty()) {
            this.agentId = generateAgentId();
        } else {
            this.agentId = tempAgentId;
        }
    }
    
    public void start() {
        log.info("Starting Monitor Agent...");
        log.info("Configuration: collector={}:{}, agentId={}, heartbeat={}s",
                collectorHost, collectorPort, agentId, heartbeatInterval);
        
        try {
            // 1. 初始化 gRPC 客户端
            grpcClient = new GrpcClient(collectorHost, collectorPort);
            grpcClient.connect();
            
            // 2. 注册到 Collector
            String hostname = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            String nodeId = grpcClient.register(agentId, hostname, ip);
            
            log.info("Agent registered successfully: hostname={}, nodeId={}", hostname, nodeId);
            
            // 3. 初始化采集器
            collectorManager = new CollectorManager(grpcClient, nodeId);
            
            // 4. 启动定时任务
            scheduler = Executors.newScheduledThreadPool(2);
            
            // 启动心跳
            scheduler.scheduleAtFixedRate(
                    () -> collectorManager.sendHeartbeat(),
                    5,
                    heartbeatInterval,
                    TimeUnit.SECONDS
            );
            
            // 启动指标采集（30秒一次）
            scheduler.scheduleAtFixedRate(
                    () -> collectorManager.collectAndSend(),
                    10,
                    30,
                    TimeUnit.SECONDS
            );
            
            running = true;
            log.info("Monitor Agent started successfully");
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            
        } catch (Exception e) {
            log.error("Failed to start Monitor Agent", e);
            throw new RuntimeException("Agent startup failed", e);
        }
    }
    
    public void stop() {
        log.info("Stopping Monitor Agent...");
        running = false;
        
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (grpcClient != null) {
            grpcClient.shutdown();
        }
        
        log.info("Monitor Agent stopped");
    }
    
    public boolean isRunning() {
        return running;
    }
    
    private String generateAgentId() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String shortUuid = UUID.randomUUID().toString().substring(0, 8);
            return hostname + "-" + shortUuid;
        } catch (Exception e) {
            return "agent-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
    
    private int parseIntEnv(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid {} value: {}, using default: {}", name, value, defaultValue);
            return defaultValue;
        }
    }
    
    public static void main(String[] args) {
        MonitorAgent agent = new MonitorAgent();
        agent.start();
        
        // 保持主线程运行
        try {
            while (agent.isRunning()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            agent.stop();
        }
    }
}
