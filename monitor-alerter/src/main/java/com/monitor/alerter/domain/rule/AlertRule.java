package com.monitor.alerter.domain.rule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.util.UUID;

@Data
public class AlertRule {
    private UUID id;
    private String name;
    private UUID projectId;
    private String refType;
    private UUID refId;
    private JsonNode triggerConf;
    private JsonNode notifyChannels;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void setTriggerConfJson(String json) {
        try {
            this.triggerConf = json != null ? MAPPER.readTree(json) : null;
        } catch (JsonProcessingException e) {
            this.triggerConf = null;
        }
    }

    public void setNotifyChannelsJson(String json) {
        try {
            this.notifyChannels = json != null ? MAPPER.readTree(json) : null;
        } catch (JsonProcessingException e) {
            this.notifyChannels = null;
        }
    }
}
