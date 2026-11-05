package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;

public class UnknownCommand extends Command {

    public UnknownCommand(LinkTrackerBot context, Update update) {
        super(context, update);
    }

    @Override
    public void execute() {
        var chatId = update.message().chat().id();
        context.sendMessage(chatId, "Неопознанная команда");
    }
}
