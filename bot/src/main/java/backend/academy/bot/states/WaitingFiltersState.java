package backend.academy.bot.states;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class WaitingFiltersState extends State {
    private static final Pattern WHITESPACES = Pattern.compile("\\s+");

    public WaitingFiltersState(LinkTrackerBot bot) {
        super(bot);
    }

    @Override
    public void handleUpdate(Update update) {
        var possibleFilters = update.message().text().trim();

        if (possibleFilters.startsWith("/")) {
            context.commandParser().handle(context, update).execute();
            return;
        }

        var chatId = update.message().chat().id();
        context.chatsDB().setFiltersById(chatId, new ArrayList<>(Arrays.asList(WHITESPACES.split(possibleFilters))));
        try {
            context.scrapperClient().addTrackedLink(chatId, context.chatsDB().chatIdToAddLinkRequest(chatId));
            context.sendMessage(chatId, LINK_ADDED_SUCCESSFULLY);
        } catch (Exception e) {
            context.sendMessage(chatId, ENTER_CORRECT_LINK);
        }
        context.chatsDB().setChatStateById(chatId, new MenuState(context));
        context.sendMessage(chatId, RETURN_TO_MENU);
        context.chatsDB().clearBufferById(chatId);
    }
}
