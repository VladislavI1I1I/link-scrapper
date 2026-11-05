package backend.academy.bot.service;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.common.model.LinkUpdate;
import org.springframework.stereotype.Service;

@Service
public class UpdateService {
    private final LinkTrackerBot bot;

    public UpdateService(LinkTrackerBot bot) {
        this.bot = bot;
    }

    public void handleUpdate(LinkUpdate update) {
        for (var id : update.tgChatIds()) {
            bot.sendMessage(id, update.description());
        }
    }
}
