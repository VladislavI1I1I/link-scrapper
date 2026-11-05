package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.TrackCommand;
import com.pengrad.telegrambot.model.Update;

public class TrackHandler extends Handler {
    @Override
    public Command handle(LinkTrackerBot context, Update update) {
        var possibleTrackCommand = update.message().text().trim();
        if (!"/track".equals(possibleTrackCommand)) {
            return checkNext(context, update);
        }
        return new TrackCommand(context, update);
    }
}
