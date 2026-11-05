package backend.academy.bot.states;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;

public class WaitingTagState extends State {

    public WaitingTagState(LinkTrackerBot bot) {
        super(bot);
    }

    @Override
    public void handleUpdate(Update update) {
        var chatId = update.message().chat().id();
        var possibleTag = update.message().text().trim();

        if (possibleTag.startsWith("/")) {
            context.commandParser().handle(context, update).execute();
            return;
        }

        context.chatsDB().setChatStateById(chatId, new WaitingFiltersState(context));
        context.chatsDB().setTagById(chatId, possibleTag);
        context.sendMessage(chatId, "Введите фильтры ссылки, или: " + LINK_ADDING_INSTRUCTIONS_MESSAGE);
    }
}
