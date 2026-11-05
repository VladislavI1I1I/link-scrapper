package backend.academy.scrapper.clients.bot;

import backend.academy.common.model.LinkUpdate;

public interface BotClientContract {
    void sendUpdate(LinkUpdate update);
}
