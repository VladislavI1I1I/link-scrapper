package backend.academy.bot.states;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;

public class StartState extends State {
    public StartState(LinkTrackerBot context) {
        super(context);
    }

    @Override
    public void handleUpdate(Update update) {
        var chatId = update.message().chat().id();

        context.scrapperClient().registerChat(chatId);
        context.sendMessage(chatId, "Здравствуйте! Введите псевдоним, по которому можно к вам обращаться");
        context.chatsDB().setChatStateById(chatId, new WaitingNicknameState(context));
    }
}
