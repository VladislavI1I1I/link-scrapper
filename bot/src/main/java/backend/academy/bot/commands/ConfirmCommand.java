package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.MenuState;
import backend.academy.bot.states.WaitingFiltersState;
import backend.academy.bot.states.WaitingTagState;
import com.pengrad.telegrambot.model.Update;

public class ConfirmCommand extends Command {
    public ConfirmCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        var chatId = update.message().chat().id();
        var chatState = context.chatsDB().getChatStateById(chatId);
        if (chatState instanceof WaitingTagState || chatState instanceof WaitingFiltersState) {
            try {
                context.scrapperClient()
                        .addTrackedLink(chatId, context.chatsDB().chatIdToAddLinkRequest(chatId));
                context.sendMessage(chatId, LINK_ADDED_SUCCESSFULLY);
            } catch (Exception e) {
                context.sendMessage(chatId, ENTER_CORRECT_LINK);
            }
            context.chatsDB().clearBufferById(chatId);
            context.chatsDB().setChatStateById(chatId, new MenuState(context));
            context.sendMessage(chatId, RETURN_TO_MENU);
        } else {
            context.sendExecuteInMenuMessage(chatId);
        }
    }
}
