package backend.academy.bot.commands.parser;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.UnknownCommand;
import com.pengrad.telegrambot.model.Update;

public abstract class Handler {
    private Handler next;

    public static Handler link(Handler first, Handler... chain) {
        Handler head = first;
        for (Handler nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract Command handle(LinkTrackerBot context, Update update);

    protected Command checkNext(LinkTrackerBot context, Update update) {
        if (next == null) {
            return new UnknownCommand(context, update);
        }
        return next.handle(context, update);
    }
}
