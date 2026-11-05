package backend.academy.bot.states;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;

public class MenuState extends State {
    public MenuState(LinkTrackerBot bot) {
        super(bot);
    }

    @Override
    public void handleUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }
        context.commandParser().handle(context, update).execute();
    }
}
