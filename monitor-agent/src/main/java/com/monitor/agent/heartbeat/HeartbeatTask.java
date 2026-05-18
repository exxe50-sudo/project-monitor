package com.monitor.agent.heartbeat;

import com.monitor.agent.grpc.proto.Heartbeat;
import com.monitor.agent.grpc.proto.HeartbeatInfo;
import com.monitor.agent.transport.GrpcClient;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

@Slf4j
@Component
public class HeartbeatTask implements Runnable {
    private final GrpcClient grpcClient;
    private final SystemInfo si = new SystemInfo();

    @Setter
    private String nodeId;

    public HeartbeatTask(GrpcClient grpcClient) {
        this.grpcClient = grpcClient;
    }

    @Override
    public void run() {
        if (nodeId == null) return;
        try {
            var mem = si.getHardware().getMemory();
            long totalMem = mem.getTotal();
            long availMem = mem.getAvailable();
            double memPct = (1.0 - (double) availMem / totalMem) * 100;

            HeartbeatInfo info = HeartbeatInfo.newBuilder()
                    .setCpuUtil(si.getHardware().getProcessor().getSystemCpuLoad(1000) * 100)
                    .setMemUsedPct(memPct)
                    .setDiskUsedPct(0)
                    .setAgentStatus("RUNNING")
                    .build();

            Heartbeat heartbeat = Heartbeat.newBuilder()
                    .setAgentId("agent")
                    .setNodeId(nodeId)
                    .setTimestamp(System.currentTimeMillis())
                    .setInfo(info)
                    .build();

            grpcClient.sendHeartbeat(heartbeat);
            log.debug("Heartbeat sent for node {}", nodeId);
        } catch (Exception e) {
            log.warn("Heartbeat failed: {}", e.getMessage());
        }
    }
}
