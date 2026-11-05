package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.DeleteCommand;
import com.pengrad.telegrambot.model.Update;

public class DeleteHandler extends Handler {
    @Override
    public Command handle(LinkTrackerBot context, Update update) {
        var possibleDeleteCommand = update.message().text().trim();
        if (!"/delete".equals(possibleDeleteCommand)) {
            return checkNext(context, update);
        }
        return new DeleteCommand(context, update);
    }
}
