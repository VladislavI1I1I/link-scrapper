package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.HelpCommand;
import com.pengrad.telegrambot.model.Update;

public class HelpHandler extends Handler {

    @Override
    public Command handle(LinkTrackerBot context, Update update) {
        var possibleHelpCommand = update.message().text().trim();
        if (!"/help".equals(possibleHelpCommand)) {
            return checkNext(context, update);
        }
        return new HelpCommand(context, update);
    }
}
