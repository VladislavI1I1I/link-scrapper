package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.UntrackCommand;
import com.pengrad.telegrambot.model.Update;

public class UntrackHandler extends Handler {

    @Override
    public Command handle(LinkTrackerBot context, Update update) {
        var possibleUntrackCommand = update.message().text().trim();
        if (!"/untrack".equals(possibleUntrackCommand)) {
            return checkNext(context, update);
        }
        return new UntrackCommand(context, update);
    }
}
