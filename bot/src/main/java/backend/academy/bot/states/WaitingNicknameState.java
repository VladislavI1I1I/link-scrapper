package backend.academy.bot.states;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;

public class WaitingNicknameState extends State {
    public WaitingNicknameState(LinkTrackerBot context) {
        super(context);
    }

    @Override
    public void handleUpdate(Update update) {
        var chatId = update.message().chat().id();
        String userName = update.message().text().trim();

        String message = "Приятно познакомиться, " + userName + "!";

        context.sendMessage(chatId, message);

        context.chatsDB().setUserNameById(chatId, userName);
        context.chatsDB().setChatStateById(chatId, new MenuState(context));
    }
}
