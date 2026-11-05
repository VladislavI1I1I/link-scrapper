package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.MenuState;
import com.pengrad.telegrambot.model.Update;

public class HelpCommand extends Command {
    public HelpCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        var chatId = update.message().chat().id();
        var chatState = context.chatsDB().getChatStateById(chatId);
        switch (chatState) {
            case MenuState menu:
                context.sendHelpMessage(chatId);
                return;
            default:
                context.sendExecuteInMenuMessage(chatId);
        }
    }
}
