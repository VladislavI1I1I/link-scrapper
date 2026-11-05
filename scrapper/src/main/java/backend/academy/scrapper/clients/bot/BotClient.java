package backend.academy.scrapper.clients.bot;

import backend.academy.common.model.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "http")
public class BotClient implements BotClientContract {

    private final BotClientApi botClientApi;

    @Override
    public void sendUpdate(LinkUpdate update) {
        log.atInfo()
                .addKeyValue("action", "send_update")
                .addKeyValue("url", update.url())
                .log("Отправляю обновление ссылки по HTTP");
        botClientApi.sendUpdate(update);
    }
}
