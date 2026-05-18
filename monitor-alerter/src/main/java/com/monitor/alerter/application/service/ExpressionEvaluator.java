package com.monitor.alerter.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ExpressionEvaluator {

    // 匹配 {item_key.function(params)} operator value
    private static final Pattern EXPR_PATTERN = Pattern.compile(
            "\\{([a-zA-Z0-9_.\\[\\],]+)\\.(\\w+)\\(([^)]*)\\)\\}\\s*(>|<|>=|<=|=|<>)\\s*([\\d.]+)"
    );

    public boolean evaluate(String expression, EvaluationContext ctx) {
        String currentItemKey = ctx.itemKey();
        double currentValue = ctx.value();

        Matcher matcher = EXPR_PATTERN.matcher(expression.replaceAll("\\s+", " "));
        if (!matcher.find()) {
            log.warn("Cannot parse expression: {}", expression);
            return false;
        }

        String itemKey = matcher.group(1);
        String function = matcher.group(2);
        String operator = matcher.group(4);
        double threshold = Double.parseDouble(matcher.group(5));

        // 检查 itemKey 是否匹配（支持通配符 *）
        if (!matchItemKey(itemKey, currentItemKey)) {
            return true; // 不匹配的 item 不触发告警
        }

        double valueToCheck = currentValue;

        // 简单函数评估（实际应从TimescaleDB取窗口数据）
        switch (function) {
            case "last" -> valueToCheck = currentValue;
            case "avg" -> valueToCheck = currentValue; // 简化：先用当前值
            case "min" -> valueToCheck = currentValue;
            case "max" -> valueToCheck = currentValue;
            default -> valueToCheck = currentValue;
        }

        return switch (operator) {
            case ">" -> valueToCheck > threshold;
            case "<" -> valueToCheck < threshold;
            case ">=" -> valueToCheck >= threshold;
            case "<=" -> valueToCheck <= threshold;
            case "=" -> Math.abs(valueToCheck - threshold) < 0.0001;
            case "<>" -> Math.abs(valueToCheck - threshold) >= 0.0001;
            default -> false;
        };
    }

    private boolean matchItemKey(String pattern, String actual) {
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*").replace("[", "\\[").replace("]", "\\]");
            return actual.matches(regex);
        }
        return pattern.equals(actual);
    }

    public record EvaluationContext(String nodeId, String itemKey, double value, Instant timestamp) {}
}
