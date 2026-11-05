package backend.academy.scrapper.configurations;

import backend.academy.common.model.TopicsInfo;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class KafkaConfig {
    private static final int PARTITIONS = 1;
    private static final short REPLICATION_FACTOR = 1;

    TopicsInfo topicsInfo;

    @Bean
    public NewTopic dlqTopic() {
        return new NewTopic(topicsInfo.dlq(), PARTITIONS, REPLICATION_FACTOR);
    }

    @Bean
    public NewTopic updatesTopic() {
        return new NewTopic(topicsInfo.updater(), PARTITIONS, REPLICATION_FACTOR);
    }
}
