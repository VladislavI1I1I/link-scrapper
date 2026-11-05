package backend.academy.bot.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.common.model.LinkUpdate;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

class LinkUpdateJsonMappingTest {
    private static final String TOPIC = "updates";

    private JsonDeserializer<LinkUpdate> deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new JsonDeserializer<>();
        deserializer.configure(
                Map.of(
                        JsonDeserializer.VALUE_DEFAULT_TYPE, LinkUpdate.class.getName(),
                        JsonDeserializer.USE_TYPE_INFO_HEADERS, false,
                        JsonDeserializer.TRUSTED_PACKAGES, "backend.academy.common.model"),
                false);
    }

    @AfterEach
    void tearDown() {
        deserializer.close();
    }

    private LinkUpdate read(String json) {
        return deserializer.deserialize(TOPIC, json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldMapValidJsonToLinkUpdate() {
        LinkUpdate update = read("""
                {"id": 1, "url": "https://github.com/enhorse/java-interview",
                 "description": "Новый issue", "tgChatIds": [101, 102]}
                """);

        assertThat(update)
                .isEqualTo(new LinkUpdate(
                        1L, "https://github.com/enhorse/java-interview", "Новый issue", List.of(101L, 102L)));
    }

    @Test
    void shouldIgnoreUnknownFields() {
        LinkUpdate update = read("""
                {"id": 1, "url": "https://github.com/enhorse/java-interview",
                 "description": "Новый issue", "tgChatIds": [101], "source": "github"}
                """);

        assertThat(update.tgChatIds()).containsExactly(101L);
    }

    @Test
    void shouldReadMessageProducedByScrapper() {
        var original =
                new LinkUpdate(7L, "https://stackoverflow.com/questions/74364929", "Новый Ответ", List.of(5L));
        try (var serializer = new JsonSerializer<LinkUpdate>()) {
            serializer.configure(Map.of(JsonSerializer.ADD_TYPE_INFO_HEADERS, false), false);

            byte[] payload = serializer.serialize(TOPIC, original);

            assertThat(deserializer.deserialize(TOPIC, payload)).isEqualTo(original);
        }
    }
}
