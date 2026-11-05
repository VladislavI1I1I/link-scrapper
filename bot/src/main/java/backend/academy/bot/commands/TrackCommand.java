package backend.academy.bot.commands;

import static backend.academy.bot.LinkTrackerBot.WARNING_ABOUT_ACCESSIBLE_LINKS;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.MenuState;
import backend.academy.bot.states.WaitingLinkToTrackState;
import com.pengrad.telegrambot.model.Update;

public class TrackCommand extends Command {
    public TrackCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        var chatId = update.message().chat().id();
        var chatState = context.chatsDB().getChatStateById(chatId);
        switch (chatState) {
            case MenuState menu:
                context.sendMessage(
                        chatId,
                        "Введите url ссылки, которую вы хотели бы отслеживать" + WARNING_ABOUT_ACCESSIBLE_LINKS);
                context.chatsDB().setChatStateById(chatId, new WaitingLinkToTrackState(context));
                return;
            default:
                context.sendExecuteInMenuMessage(chatId);
        }
    }
}
