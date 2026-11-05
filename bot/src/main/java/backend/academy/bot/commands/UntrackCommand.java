package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.MenuState;
import backend.academy.bot.states.WaitingLinkToUntrackState;
import com.pengrad.telegrambot.model.Update;

public class UntrackCommand extends Command {
    public UntrackCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        Long chatId = update.message().chat().id();
        var chatState = context.chatsDB().getChatStateById(chatId);
        switch (chatState) {
            case MenuState menu:
                context.sendMessage(chatId, "Введите url ссылки, которую вы хотели бы перестать отслеживать");
                context.chatsDB().setChatStateById(chatId, new WaitingLinkToUntrackState(context));
                return;
            default:
                context.sendExecuteInMenuMessage(chatId);
        }
    }
}
