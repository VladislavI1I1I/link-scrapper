package backend.academy.bot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import backend.academy.bot.AbstractBotIntegrationTest;
import backend.academy.common.model.TopicsInfo;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.kafka.KafkaContainer;

class KafkaUpdatesIntegrationTest extends AbstractBotIntegrationTest {
    private static final long TIMEOUT_MS = 30_000L;
    private static final Duration DLQ_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    @Autowired
    private KafkaContainer kafka;

    @Autowired
    private TopicsInfo topics;

    @Test
    void validMessageShouldBeDeliveredToAllSubscribedChats() throws Exception {
        send("valid-1", """
                {"id": 1, "url": "https://github.com/enhorse/java-interview",
                 "description": "Новый issue", "tgChatIds": [101, 102]}
                """);

        verify(bot, timeout(TIMEOUT_MS)).sendMessage(101L, "Новый issue");
        verify(bot, timeout(TIMEOUT_MS)).sendMessage(102L, "Новый issue");
    }

    @Test
    void unparsableMessageShouldBeRedirectedToDlq() throws Exception {
        String key = "broken-json-" + UUID.randomUUID();

        send(key, "это не json");

        assertThat(awaitDlqRecord(key)).isPresent();
    }

    @Test
    void invalidMessageShouldBeRedirectedToDlq() throws Exception {
        String key = "no-chats-" + UUID.randomUUID();

        send(key, """
                {"id": 2, "url": "https://github.com/enhorse/java-interview", "description": "Новый issue"}
                """);

        assertThat(awaitDlqRecord(key)).isPresent();
        verify(bot, after(1000).never()).sendMessage(anyLong(), anyString());
    }

    private void send(String key, String value) throws Exception {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        try (var producer = new KafkaProducer<String, String>(props)) {
            producer.send(new ProducerRecord<>(topics.updater(), key, value)).get();
        }
    }

    private Optional<ConsumerRecord<String, String>> awaitDlqRecord(String key) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlq-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.METADATA_MAX_AGE_CONFIG, 1000,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        try (var consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(List.of(topics.dlq()));
            long deadline = System.nanoTime() + DLQ_TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(POLL_INTERVAL)) {
                    if (key.equals(record.key())) {
                        return Optional.of(record);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
