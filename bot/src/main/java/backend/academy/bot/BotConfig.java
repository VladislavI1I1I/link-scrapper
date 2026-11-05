package backend.academy.bot;

import backend.academy.bot.commands.parser.ConfirmHandler;
import backend.academy.bot.commands.parser.DeleteHandler;
import backend.academy.bot.commands.parser.Handler;
import backend.academy.bot.commands.parser.HelpHandler;
import backend.academy.bot.commands.parser.ListHandler;
import backend.academy.bot.commands.parser.MenuHandler;
import backend.academy.bot.commands.parser.TrackHandler;
import backend.academy.bot.commands.parser.UntrackHandler;
import backend.academy.bot.storage.ChatsDB;
import com.pengrad.telegrambot.TelegramBot;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record BotConfig(@NotEmpty String telegramToken, @NotEmpty String baseUrl) {
    @Bean
    ChatsDB chatsDB() {
        return new ChatsDB();
    }

    @Bean
    TelegramBot bot() {
        return new TelegramBot(telegramToken);
    }

    @Bean
    Handler CommandParser() {
        return Handler.link(
                new DeleteHandler(),
                new HelpHandler(),
                new ListHandler(),
                new MenuHandler(),
                new TrackHandler(),
                new UntrackHandler(),
                new ConfirmHandler());
    }
}
