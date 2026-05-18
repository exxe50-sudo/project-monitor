package com.monitor.agent.collector;

import com.monitor.agent.grpc.proto.Metric;
import java.util.List;

public interface Collector {
    String name();
    List<Metric> collect();
}
