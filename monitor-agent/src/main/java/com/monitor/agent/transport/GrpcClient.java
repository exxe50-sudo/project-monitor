package com.monitor.agent.transport;

import com.monitor.agent.grpc.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
public class GrpcClient {
    private ManagedChannel channel;
    private AgentServiceGrpc.AgentServiceStub asyncStub;
    private AgentServiceGrpc.AgentServiceBlockingStub blockingStub;
    private StreamObserver<MetricReport> metricStream;
    private StreamObserver<Heartbeat> heartbeatStream;

    @Value("${agent.collector-host:localhost}")
    private String host;

    @Value("${agent.collector-port:9090}")
    private int port;

    public void connect() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .maxInboundMessageSize(10 * 1024 * 1024)
                .build();
        asyncStub = AgentServiceGrpc.newStub(channel);
        blockingStub = AgentServiceGrpc.newBlockingStub(channel);
        log.info("gRPC channel connected to {}:{}", host, port);
    }

    public RegisterResponse register(RegisterRequest request) {
        return blockingStub.register(request);
    }

    public void startMetricStream(Consumer<MetricAck> onAck) {
        metricStream = asyncStub.reportMetrics(new StreamObserver<MetricAck>() {
            @Override
            public void onNext(MetricAck ack) {
                onAck.accept(ack);
            }
            @Override
            public void onError(Throwable t) {
                log.error("Metric stream error", t);
            }
            @Override
            public void onCompleted() {
                log.info("Metric stream completed");
            }
        });
    }

    public void sendMetric(MetricReport report) {
        if (metricStream != null) {
            metricStream.onNext(report);
        }
    }

    public void startHeartbeatStream(Consumer<HeartbeatAck> onAck) {
        heartbeatStream = asyncStub.heartbeat(new StreamObserver<HeartbeatAck>() {
            @Override
            public void onNext(HeartbeatAck ack) {
                onAck.accept(ack);
            }
            @Override
            public void onError(Throwable t) {
                log.error("Heartbeat stream error", t);
            }
            @Override
            public void onCompleted() {
                log.info("Heartbeat stream completed");
            }
        });
    }

    public void sendHeartbeat(Heartbeat heartbeat) {
        if (heartbeatStream != null) {
            heartbeatStream.onNext(heartbeat);
        }
    }

    public void shutdown() {
        if (metricStream != null) metricStream.onCompleted();
        if (heartbeatStream != null) heartbeatStream.onCompleted();
        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
