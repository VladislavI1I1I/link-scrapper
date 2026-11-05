package backend.academy.bot;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.parser.Handler;
import backend.academy.bot.states.StartState;
import backend.academy.bot.storage.ChatsDB;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LinkTrackerBot {
    @Getter
    private final ChatsDB chatsDB;

    @Getter
    private final ScrapperClient scrapperClient;

    private final TelegramBot bot;

    @Getter
    private final Handler commandParser;

    public static final String WARNING_ABOUT_ACCESSIBLE_LINKS =
            "\n Важно: Cейчас поддерживаются только следующие ccылки:\nРепозитории GitHub\nВопросы StackOverflow";

    private String helpMessage;

    public LinkTrackerBot(ScrapperClient scrapperClient, TelegramBot bot, ChatsDB chatsDB, Handler commandParser) {
        this.scrapperClient = scrapperClient;
        this.chatsDB = chatsDB;
        this.bot = bot;
        this.commandParser = commandParser;
    }

    public void sendMessage(Long chatId, String message) {
        bot.execute(new SendMessage(chatId, message));
    }

    public void sendHelpMessage(Long chatId) {
        bot.execute(new SendMessage(chatId, helpMessage));
    }

    public void sendExecuteInMenuMessage(Long chatId) {
        bot.execute(new SendMessage(chatId, "Чтобы выполнить данную команду, необходимо находиться в меню"));
    }

    @PostConstruct
    private void setBot() {
        bot.execute(new SetMyCommands(registerCommands()));
        bot.setUpdatesListener(this::processUpdates, e -> log.atError().log("Error found: {}", e.getMessage()));
    }

    private int processUpdates(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null) {
                continue;
            }

            Long chatId = update.message().chat().id();

            if (update.message().text() == null) {
                sendMessage(chatId, "Введите текстовую команду");
                continue;
            }

            if (!chatsDB.contains(chatId)) {
                chatsDB.putNewUserById(chatId, new StartState(this));
            }

            chatsDB.getChatStateById(chatId).handleUpdate(update);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private BotCommand[] registerCommands() {
        BotCommand[] commands = new BotCommand[] {
            new BotCommand("/start", "Запустить бота"),
            new BotCommand("/list", "Вывести все отслеживаемые ссылки"),
            new BotCommand("/track", "Отслеживает введенную ссылку"),
            new BotCommand("/untrack", "Перестает отслеживать введенную ссылку"),
            new BotCommand("/nickname", "Cменить псевдоним"),
            new BotCommand("/menu", "Вернуться в меню"),
            new BotCommand("/delete", "Удалить чат"),
            new BotCommand("/help", "Описание всех команд"),
        };
        helpMessage = Arrays.stream(commands)
                        .map(command -> MessageFormat.format("{0} - {1}", command.command(), command.description()))
                        .collect(Collectors.joining("\n"))
                + WARNING_ABOUT_ACCESSIBLE_LINKS;
        return commands;
    }
}
