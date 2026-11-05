package backend.academy.common.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record TopicsInfo(String updater, String dlq) {}
