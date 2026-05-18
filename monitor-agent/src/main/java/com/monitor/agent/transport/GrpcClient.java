package com.monitor.agent.transport;

import com.monitor.agent.grpc.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * gRPC 客户端 - 负责与 Collector 通信
 */
public class GrpcClient {
    private static final Logger log = LoggerFactory.getLogger(GrpcClient.class);
    
    private ManagedChannel channel;
    private AgentServiceGrpc.AgentServiceStub asyncStub;
    private AgentServiceGrpc.AgentServiceBlockingStub blockingStub;
    private StreamObserver<MetricReport> metricStream;
    private StreamObserver<Heartbeat> heartbeatStream;
    
    private final String host;
    private final int port;
    
    public GrpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public void connect() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .maxInboundMessageSize(10 * 1024 * 1024)
                .build();
        asyncStub = AgentServiceGrpc.newStub(channel);
        blockingStub = AgentServiceGrpc.newBlockingStub(channel);
        log.info("gRPC channel connected to {}:{}", host, port);
    }
    
    public String register(String agentId, String hostname, String ipAddress) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setAgentId(agentId)
                .setHostname(hostname)
                .setIpAddress(ipAddress)
                .setOsType(System.getProperty("os.name"))
                .setOsVersion(System.getProperty("os.version"))
                .setAgentVersion("1.0.0")
                .build();
        
        RegisterResponse response = blockingStub.register(request);
        log.info("Registered with collector, nodeId={}", response.getNodeId());
        return response.getNodeId();
    }
    
    public void startMetricStream() {
        metricStream = asyncStub.reportMetrics(new StreamObserver<MetricAck>() {
            @Override
            public void onNext(MetricAck ack) {
                log.debug("Metric ack received: count={}", ack.getReceivedCount());
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
        if (metricStream == null) {
            startMetricStream();
        }
        if (metricStream != null) {
            metricStream.onNext(report);
        }
    }
    
    public void startHeartbeatStream() {
        heartbeatStream = asyncStub.heartbeatStream(new StreamObserver<HeartbeatAck>() {
            @Override
            public void onNext(HeartbeatAck ack) {
                log.debug("Heartbeat ack received");
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
        if (heartbeatStream == null) {
            startHeartbeatStream();
        }
        if (heartbeatStream != null) {
            heartbeatStream.onNext(heartbeat);
        }
    }
    
    public void shutdown() {
        if (metricStream != null) {
            try {
                metricStream.onCompleted();
            } catch (Exception e) {
                log.warn("Error closing metric stream", e);
            }
        }
        if (heartbeatStream != null) {
            try {
                heartbeatStream.onCompleted();
            } catch (Exception e) {
                log.warn("Error closing heartbeat stream", e);
            }
        }
        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
        }
        log.info("gRPC channel closed");
    }
}
