package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.ListCommand;
import com.pengrad.telegrambot.model.Update;

public class ListHandler extends Handler {
    @Override
    public Command handle(LinkTrackerBot context, Update update) {
        var possibleListCommand = update.message().text().trim();
        if (!"/list".equals(possibleListCommand)) {
            return checkNext(context, update);
        }
        return new ListCommand(context, update);
    }
}
