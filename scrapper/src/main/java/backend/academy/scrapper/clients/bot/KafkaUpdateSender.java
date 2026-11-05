package backend.academy.scrapper.clients.bot;

import backend.academy.common.model.LinkUpdate;
import backend.academy.common.model.TopicsInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "kafka", matchIfMissing = true)
public class KafkaUpdateSender implements BotClientContract {

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private final TopicsInfo topicsInfo;

    @Override
    public void sendUpdate(LinkUpdate update) {
        String key = String.valueOf(update.id());
        kafkaTemplate
                .send(topicsInfo.updater(), key, update)
                .thenAccept(result -> log.atInfo()
                        .addKeyValue("topic", result.getRecordMetadata().topic())
                        .addKeyValue("offset", result.getRecordMetadata().offset())
                        .log("Обновление отправлено в Kafka"))
                .exceptionally(ex -> {
                    log.atError()
                            .setCause(ex)
                            .addKeyValue("url", update.url())
                            .log("Не удалось отправить обновление, перенаправляю в {}", topicsInfo.dlq());
                    var unused = kafkaTemplate.send(topicsInfo.dlq(), key, update);
                    return null;
                });
    }
}
