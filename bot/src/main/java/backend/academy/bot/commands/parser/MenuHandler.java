package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.MenuCommand;
import com.pengrad.telegrambot.model.Update;

public class MenuHandler extends Handler {

    @Override
    public Command handle(LinkTrackerBot context, Update update) {
        var possibleMenuCommand = update.message().text().trim();
        if (!"/menu".equals(possibleMenuCommand)) {
            return checkNext(context, update);
        }
        return new MenuCommand(context, update);
    }
}
