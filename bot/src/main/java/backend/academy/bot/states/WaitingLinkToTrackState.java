package backend.academy.bot.states;

import static backend.academy.common.utils.URLValidator.isValidURL;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;

public class WaitingLinkToTrackState extends State {

    public WaitingLinkToTrackState(LinkTrackerBot bot) {
        super(bot);
    }

    @Override
    public void handleUpdate(Update update) {
        String possibleLink = update.message().text().trim();

        if (possibleLink.startsWith("/")) {
            context.commandParser().handle(context, update).execute();
            return;
        }

        var chatId = update.message().chat().id();
        if (!isValidURL(possibleLink)) {
            context.sendMessage(chatId, ENTER_CORRECT_LINK);
            return;
        }
        context.chatsDB().setLinkById(chatId, possibleLink);
        context.chatsDB().setChatStateById(chatId, new WaitingTagState(context));
        context.sendMessage(chatId, "Введите теги ссылки, или:" + LINK_ADDING_INSTRUCTIONS_MESSAGE);
    }
}
