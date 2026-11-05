package backend.academy.bot.states;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.common.model.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Update;

public class WaitingLinkToUntrackState extends State {

    public WaitingLinkToUntrackState(LinkTrackerBot bot) {
        super(bot);
    }

    @Override
    public void handleUpdate(Update update) {

        String possibleLink = update.message().text().trim();

        if (possibleLink.startsWith("/")) {
            context.commandParser().handle(context, update).execute();
            return;
        }

        Long chatId = update.message().chat().id();
        try {
            context.scrapperClient().removeTrackedLink(chatId, new RemoveLinkRequest(possibleLink));
            context.sendMessage(chatId, "Ссылка успешно удалена!");

            context.sendMessage(chatId, RETURN_TO_MENU);
            context.chatsDB().setChatStateById(chatId, new MenuState(context));
        } catch (Exception e) {
            context.sendMessage(chatId, ENTER_CORRECT_LINK);
        }
    }
}
