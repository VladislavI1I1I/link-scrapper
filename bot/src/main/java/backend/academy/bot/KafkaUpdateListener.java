package backend.academy.bot;

import backend.academy.bot.service.UpdateService;
import backend.academy.common.model.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUpdateListener {
    private final UpdateService updateService;

    @KafkaListener(topics = "${app.kafka.topics.updater}")
    public void getMessage(LinkUpdate update) {
        validate(update);
        log.atInfo().addKeyValue("url", update.url()).log("Получено обновление из Kafka");
        updateService.handleUpdate(update);
    }

    private static void validate(LinkUpdate update) {
        if (update.url() == null
                || update.description() == null
                || update.tgChatIds() == null
                || update.tgChatIds().isEmpty()) {
            throw new IllegalArgumentException("Некорректное обновление: " + update);
        }
    }
}
