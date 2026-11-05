package backend.academy.scrapper.clients.bot;

import backend.academy.common.model.LinkUpdate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface BotClientApi {

    @PostExchange("/updates")
    void sendUpdate(@RequestBody LinkUpdate update);
}
