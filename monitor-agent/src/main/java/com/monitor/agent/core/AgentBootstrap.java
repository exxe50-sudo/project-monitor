package com.monitor.agent.core;

import com.monitor.agent.collector.CollectorManager;
import com.monitor.agent.heartbeat.HeartbeatTask;
import com.monitor.agent.transport.GrpcClient;
import com.monitor.agent.grpc.proto.RegisterRequest;
import com.monitor.agent.grpc.proto.RegisterResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentBootstrap {
    private final GrpcClient grpcClient;
    private final CollectorManager collectorManager;
    private final HeartbeatTask heartbeatTask;

    @Value("${agent.agent-id:auto}")
    private String agentId;

    private String nodeId;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    @PostConstruct
    public void start() {
        try {
            // 1. 获取本机信息
            SystemInfo si = new SystemInfo();
            String hostname = InetAddress.getLocalHost().getHostName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            String osType = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");

            if ("auto".equals(agentId)) {
                agentId = hostname + "-" + UUID.randomUUID().toString().substring(0, 8);
            }

            // 2. 连接 gRPC
            grpcClient.connect();

            // 3. 注册
            RegisterRequest request = RegisterRequest.newBuilder()
                    .setAgentId(agentId)
                    .setHostname(hostname)
                    .setIpAddress(ip)
                    .setOsType(osType)
                    .setOsVersion(osVersion)
                    .setAgentVersion("1.0.0")
                    .build();
            RegisterResponse response = grpcClient.register(request);
            this.nodeId = response.getNodeId();
            this.collectorManager.setNodeId(nodeId);
            this.heartbeatTask.setNodeId(nodeId);
            log.info("Agent registered successfully: hostname={}, nodeId={}", hostname, nodeId);

            // 4. 启动心跳
            scheduler.scheduleAtFixedRate(heartbeatTask, 5, 5, TimeUnit.SECONDS);

            // 5. 启动采集
            scheduler.scheduleAtFixedRate(collectorManager, 5, 30, TimeUnit.SECONDS);

            log.info("Agent started successfully");
        } catch (Exception e) {
            log.error("Failed to start agent", e);
        }
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdown();
        grpcClient.shutdown();
        log.info("Agent stopped");
    }
}
