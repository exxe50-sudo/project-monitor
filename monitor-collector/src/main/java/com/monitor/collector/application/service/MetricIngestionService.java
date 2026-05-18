package com.monitor.collector.application.service;

import com.monitor.collector.grpc.proto.Metric;
import com.monitor.collector.grpc.proto.MetricReport;
import com.monitor.collector.infrastructure.messaging.MetricProducer;
import com.monitor.collector.infrastructure.persistence.MetricBatchWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class MetricIngestionService {
    private final MetricBatchWriter batchWriter;
    private final MetricProducer metricProducer;
    private final BlockingQueue<MetricPoint> buffer = new LinkedBlockingQueue<>(10000);

    public MetricIngestionService(MetricBatchWriter batchWriter, MetricProducer metricProducer) {
        this.batchWriter = batchWriter;
        this.metricProducer = metricProducer;
        startFlushThread();
    }

    private void startFlushThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    flush();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "metric-flush-thread");
        thread.setDaemon(true);
        thread.start();
    }

    public void ingest(MetricReport report) {
        String nodeId = report.getNodeId();
        for (Metric metric : report.getMetricsList()) {
            MetricPoint point = new MetricPoint(
                    nodeId, null, metric.getItemKey(), metric.getValue(),
                    Instant.ofEpochMilli(report.getTimestamp())
            );
            buffer.offer(point);
        }
    }

    private void flush() {
        List<MetricPoint> batch = new ArrayList<>();
        buffer.drainTo(batch, 500);
        if (!batch.isEmpty()) {
            // 批量写入数据库
            batchWriter.writeBatch(batch);
            // 发送到消息队列
            for (MetricPoint point : batch) {
                metricProducer.send(point);
            }
            log.debug("Flushed {} metrics", batch.size());
        }
    }

    public record MetricPoint(String nodeId, String serviceId, String itemKey, double value, Instant ts) {}
}
