package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.MenuState;
import backend.academy.common.model.LinkResponse;
import backend.academy.common.model.ListLinksResponse;
import com.pengrad.telegrambot.model.Update;
import java.util.stream.Collectors;

public class ListCommand extends Command {
    public ListCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        var chatId = update.message().chat().id();
        var chatState = context.chatsDB().getChatStateById(chatId);
        switch (chatState) {
            case MenuState menu:
                try {
                    ListLinksResponse trackedLinks = context.scrapperClient().getAllTrackedLinks(chatId);
                    if (trackedLinks == null || trackedLinks.links().isEmpty()) {
                        context.sendMessage(chatId, "Нет отслеживаемых ссылок");
                        return;
                    }
                    String allLinksMessage =
                            trackedLinks.links().stream().map(LinkResponse::url).collect(Collectors.joining("\n"));
                    context.sendMessage(chatId, "Список доступных ссылок \n" + allLinksMessage);
                } catch (Exception e) {
                    context.sendMessage(chatId, "Нет отслеживаемых ссылок");
                }
                return;
            default:
                context.sendExecuteInMenuMessage(chatId);
        }
    }
}
