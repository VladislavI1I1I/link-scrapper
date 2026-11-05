package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.ConfirmCommand;
import com.pengrad.telegrambot.model.Update;

public class ConfirmHandler extends Handler {
    @Override
    public Command handle(LinkTrackerBot context, Update update) {
        var possibleConfirmCommand = update.message().text().trim();
        if (!"/confirm".equals(possibleConfirmCommand)) {
            return checkNext(context, update);
        }
        return new ConfirmCommand(context, update);
    }
}
