package backend.academy.bot.commands;

import backend.academy.bot.LinkTrackerBot;
import backend.academy.bot.states.State;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class Command {
    public static final String RETURN_TO_MENU = State.RETURN_TO_MENU;
    public static final String ENTER_CORRECT_LINK = State.ENTER_CORRECT_LINK;
    public static final String LINK_ADDED_SUCCESSFULLY = State.LINK_ADDED_SUCCESSFULLY;

    public LinkTrackerBot context;
    public Update update;

    public abstract void execute();
}
