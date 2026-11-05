package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.MenuState;
import com.pengrad.telegrambot.model.Update;

public class MenuCommand extends Command {
    public MenuCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        var chatId = update.message().chat().id();
        var chatState = context.chatsDB().getChatStateById(chatId);
        switch (chatState) {
            case MenuState menu:
                context.sendMessage(chatId, "Вы уже в меню");
                return;
            default:
                context.chatsDB().setChatStateById(chatId, new MenuState(context));
                context.sendMessage(chatId, RETURN_TO_MENU);
                context.chatsDB().clearBufferById(chatId);
        }
    }
}
