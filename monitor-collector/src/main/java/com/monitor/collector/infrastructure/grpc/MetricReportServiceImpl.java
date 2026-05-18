package com.monitor.collector.infrastructure.grpc;

import com.monitor.collector.application.service.MetricIngestionService;
import com.monitor.collector.grpc.proto.*;
import com.monitor.collector.infrastructure.redis.ConnectionStateManager;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricReportServiceImpl extends AgentServiceGrpc.AgentServiceImplBase {

    private final MetricIngestionService ingestionService;
    private final ConnectionStateManager connectionManager;

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        String nodeId = UUID.randomUUID().toString();
        log.info("Agent registered: hostname={}, ip={}, assigned nodeId={}",
                request.getHostname(), request.getIpAddress(), nodeId);

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
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
