package backend.academy.bot.states;

import backend.academy.bot.LinkTrackerBot;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class State {
    public static final String RETURN_TO_MENU = "Вы вернулись в меню";
    public static final String ENTER_CORRECT_LINK = "Введите корректную ссылку";
    public static final String LINK_ADDED_SUCCESSFULLY = "Ссылка успешно добавлена!";
    public static final String LINK_ADDING_INSTRUCTIONS_MESSAGE =
            "\n/menu, чтобы вернуться в меню\n /confirm чтобы добавить ссылку в отслеживаемые";

    LinkTrackerBot context;

    public abstract void handleUpdate(Update update);

    protected boolean checkAndHandleMenuCommand(Update update) {
        var chatId = update.message().chat().id();
        var possibleFilters = update.message().text().trim();

        if ("/menu".equals(possibleFilters)) {
            context.chatsDB().setChatStateById(chatId, new MenuState(context));
            context.sendMessage(chatId, RETURN_TO_MENU);
            context.chatsDB().clearBufferById(chatId);
            return true;
        }
        return false;
    }
}
