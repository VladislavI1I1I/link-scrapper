package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.MenuState;
import com.pengrad.telegrambot.model.Update;

public class DeleteCommand extends Command {
    public DeleteCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        var chatId = update.message().chat().id();
        var chatState = context.chatsDB().getChatStateById(chatId);
        switch (chatState) {
            case MenuState menu:
                context.sendMessage(chatId, "До новых встреч!");
                context.chatsDB().deleteUserById(chatId);
                context.scrapperClient().deleteChat(chatId);
                return;
            default:
                context.sendExecuteInMenuMessage(chatId);
        }
    }
}
