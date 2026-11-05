package backend.academy.bot;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.MenuCommand;
import backend.academy.bot.commands.parser.ConfirmHandler;
import backend.academy.bot.commands.parser.DeleteHandler;
import backend.academy.bot.commands.parser.Handler;
import backend.academy.bot.commands.parser.HelpHandler;
import backend.academy.bot.commands.parser.ListHandler;
import backend.academy.bot.commands.parser.MenuHandler;
import backend.academy.bot.commands.parser.TrackHandler;
import backend.academy.bot.commands.parser.UntrackHandler;
import backend.academy.bot.states.MenuState;
import backend.academy.bot.states.WaitingLinkToTrackState;
import backend.academy.bot.states.WaitingTagState;
import backend.academy.bot.storage.ChatsDB;
import backend.academy.common.model.LinkResponse;
import backend.academy.common.model.ListLinksResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class BotApplicationTests {
    LinkTrackerBot mockBot;
    Update mockUpdate;
    Message mockMessage;
    Chat mockChat;
    ChatsDB mockDb;
    long mockId = 393865234L;
    static Handler mockHandler;

    @BeforeAll
    static void setup() {
        mockHandler = Handler.link(
                new DeleteHandler(),
                new HelpHandler(),
                new ListHandler(),
                new MenuHandler(),
                new TrackHandler(),
                new UntrackHandler(),
                new ConfirmHandler());
    }

    @BeforeEach
    void setUp() {
        mockBot = mock(LinkTrackerBot.class);
        mockUpdate = mock(Update.class);
        mockMessage = mock(Message.class);
        mockChat = mock(Chat.class);
        mockDb = mock(ChatsDB.class);

        when(mockBot.commandParser()).thenReturn(mockHandler);
        when(mockChat.id()).thenReturn(mockId);
        when(mockUpdate.message()).thenReturn(mockMessage);
        when(mockMessage.chat()).thenReturn(mockChat);
    }

    @Test
    void contextLoads() {}

    @Test
    void parsingLinkTest() {
        when(mockMessage.text()).thenReturn("https://stackoverflow.com/questions/74364929");
        when(mockBot.chatsDB()).thenReturn(mockDb);

        WaitingLinkToTrackState state = new WaitingLinkToTrackState(mockBot);
        mockDb.putNewUserById(mockId, state);

        state.handleUpdate(mockUpdate);

        verify(mockBot).sendMessage(eq(mockId), contains("Введите теги ссылки"));
    }

    @Test
    void warningIfCommandIsNotDefinedTest() {
        when(mockMessage.text()).thenReturn("Преврати кусок холста в шедевр искусства");
        when(mockBot.chatsDB()).thenReturn(mockDb);

        var state = new MenuState(mockBot);
        mockDb.putNewUserById(mockId, state);

        state.handleUpdate(mockUpdate);

        verify(mockBot).sendMessage(eq(mockId), contains("Неопознанная команда"));
    }

    @Test
    void listCommandTest() {
        ScrapperClient mockClient = mock(ScrapperClient.class);
        ResponseEntity<ListLinksResponse> mockListLinksResponse = mock(String.valueOf(ListLinksResponse.class));

        LinkResponse fakeLink = new LinkResponse(12345L, "CoolLink", "", new ArrayList<>());
        LinkResponse secondFakeLink = new LinkResponse(12346L, "CoolerLink", "", new ArrayList<>());

        ArrayList<LinkResponse> fakeLinks = new ArrayList<>();
        fakeLinks.add(fakeLink);
        fakeLinks.add(secondFakeLink);
        ListLinksResponse fakeResponse = new ListLinksResponse(fakeLinks, fakeLinks.size());

        when(mockMessage.text()).thenReturn("/list");
        when(mockBot.chatsDB()).thenReturn(mockDb);
        when(mockBot.scrapperClient()).thenReturn(mockClient);
        when(mockClient.getAllTrackedLinks(mockId)).thenReturn(fakeResponse);
        when(mockListLinksResponse.getBody()).thenReturn(fakeResponse);
        var state = new MenuState(mockBot);
        when(mockDb.getChatStateById(mockId)).thenReturn(state);
        mockDb.putNewUserById(mockId, state);

        state.handleUpdate(mockUpdate);

        verify(mockBot).sendMessage(eq(mockId), contains("Список доступных ссылок \nCoolLink\nCoolerLink"));
    }

    @Test
    void invalidLinkTest() {
        when(mockMessage.text()).thenReturn("not a link");
        when(mockBot.chatsDB()).thenReturn(mockDb);

        WaitingLinkToTrackState state = new WaitingLinkToTrackState(mockBot);
        mockDb.putNewUserById(mockId, state);

        state.handleUpdate(mockUpdate);

        verify(mockBot).sendMessage(eq(mockId), contains("Введите корректную ссылку"));
    }

    @Test
    void menuCommandReturnsToMenuTest() {
        when(mockBot.chatsDB()).thenReturn(mockDb);
        when(mockDb.getChatStateById(mockId)).thenReturn(new WaitingTagState(mockBot));

        new MenuCommand(mockBot, mockUpdate).execute();

        verify(mockBot).sendMessage(eq(mockId), contains("Вы вернулись в меню"));
    }
}
