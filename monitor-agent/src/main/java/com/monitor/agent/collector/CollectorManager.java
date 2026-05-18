package com.monitor.agent.collector;

import com.monitor.agent.grpc.proto.Metric;
import com.monitor.agent.grpc.proto.MetricReport;
import com.monitor.agent.transport.GrpcClient;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CollectorManager implements Runnable {
    private final GrpcClient grpcClient;
    private final List<Collector> collectors;

    @Setter
    private String nodeId;

    public CollectorManager(GrpcClient grpcClient, List<Collector> collectors) {
        this.grpcClient = grpcClient;
        this.collectors = collectors;
    }

    @Override
    public void run() {
        if (nodeId == null) return;
        try {
            List<Metric> allMetrics = new ArrayList<>();
            for (Collector collector : collectors) {
                try {
                    allMetrics.addAll(collector.collect());
                } catch (Exception e) {
                    log.warn("Collector {} failed: {}", collector.name(), e.getMessage());
                }
            }

            if (!allMetrics.isEmpty()) {
                MetricReport report = MetricReport.newBuilder()
                        .setAgentId("agent")
                        .setNodeId(nodeId)
                        .setTimestamp(System.currentTimeMillis())
                        .addAllMetrics(allMetrics)
                        .build();
                grpcClient.sendMetric(report);
                log.debug("Sent {} metrics", allMetrics.size());
            }
        } catch (Exception e) {
            log.error("CollectorManager run error", e);
        }
    }
}
