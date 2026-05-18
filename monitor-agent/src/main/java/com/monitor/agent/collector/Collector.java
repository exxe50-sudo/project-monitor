package com.monitor.agent.collector;

import com.monitor.agent.grpc.proto.Metric;
import java.util.List;

/**
 * 采集器接口 - 类似 Zabbix Agent 的采集项
 */
public interface Collector {
    /**
     * 采集器名称
     */
    String name();
    
    /**
     * 执行采集，返回指标列表
     */
    List<Metric> collect();
}
