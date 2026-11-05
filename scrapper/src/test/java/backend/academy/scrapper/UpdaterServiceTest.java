package backend.academy.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.scrapper.clients.bot.BotClientContract;
import backend.academy.scrapper.clients.update.checkers.UpdateCheckingClient;
import backend.academy.scrapper.model.SubscriptionData;
import backend.academy.scrapper.model.WebsiteUpdateInfo;
import backend.academy.scrapper.model.github.GitHubUser;
import backend.academy.scrapper.model.github.GithubInfo;
import backend.academy.scrapper.services.UpdaterService;
import backend.academy.scrapper.services.link.services.LinkService;
import backend.academy.scrapper.storage.DatabaseService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdaterServiceTest {

    @Mock
    private DatabaseService databaseServiceMock;

    @Mock
    private BotClientContract botClientMock;

    @Mock
    private LinkCheckerAndUpdater linkCheckerMock;

    @Mock
    private LinkService linkServiceMock;

    @Mock
    private UpdateCheckingClient updateCheckingClient;

    @InjectMocks
    private UpdaterService updaterService;

    @Test
    void updaterServiceTest() {

        String githubTestUrl = "https://github.com/test/repo";
        LocalDateTime newTime = LocalDateTime.of(2023, 2, 1, 12, 0);
        LocalDateTime oldTime = LocalDateTime.of(2022, 2, 1, 12, 0);
        SubscriptionData subscriptionData = new SubscriptionData(15L, new ArrayList<>(), oldTime);
        WebsiteUpdateInfo newInfo = new WebsiteUpdateInfo("", "", "", newTime, "");
        when(linkCheckerMock.checkAndGetUpdate(githubTestUrl)).thenReturn(Optional.of(newInfo));
        when(databaseServiceMock.getLinks(0L)).thenReturn(List.of(githubTestUrl));
        when(databaseServiceMock.getLinks(1L)).thenReturn(List.of());
        when(databaseServiceMock.getLinkSubscriptionData(githubTestUrl)).thenReturn(subscriptionData);
        updaterService.updateInfo();

        verify(databaseServiceMock).setUserLinkUpdateTime(githubTestUrl, newTime);
        verify(botClientMock).sendUpdate(any());
    }

    @Test
    void noUpdatesTest() {
        String githubTestUrl = "https://github.com/test/repo";
        LocalDateTime time = LocalDateTime.of(2023, 2, 1, 12, 0);
        SubscriptionData subscriptionData = new SubscriptionData(15L, new ArrayList<>(), time);
        WebsiteUpdateInfo sameInfo = new WebsiteUpdateInfo("", "", "", time, "");
        when(linkCheckerMock.checkAndGetUpdate(githubTestUrl)).thenReturn(Optional.of(sameInfo));
        when(databaseServiceMock.getLinks(0L)).thenReturn(List.of(githubTestUrl));
        when(databaseServiceMock.getLinks(1L)).thenReturn(List.of());
        when(databaseServiceMock.getLinkSubscriptionData(githubTestUrl)).thenReturn(subscriptionData);
        updaterService.updateInfo();

        verify(databaseServiceMock, never()).setUserLinkUpdateTime(any(), any());
        verify(botClientMock, never()).sendUpdate(any());
    }

    @Test
    public void updateMessagesTest() {
        LocalDateTime localtime = LocalDateTime.of(2023, 2, 1, 12, 0);
        ZonedDateTime time = ZonedDateTime.of(localtime, ZoneId.of("Europe/Berlin"));
        GithubInfo githubInfo = new GithubInfo("coolTitle", "someInfo", new GitHubUser("coolUser"), time);
        WebsiteUpdateInfo commitInfo = new WebsiteUpdateInfo(githubInfo, "commit");
        WebsiteUpdateInfo issueInfo = new WebsiteUpdateInfo(githubInfo, "issue");
        Assertions.assertEquals(
                """
        Обновление!
        Заголовок: coolTitle
        Время обновления: 2023-02-01T12:00
        Имя пользователя coolUser
        Новый commit: someInfo
        """,
                commitInfo.toString());
        Assertions.assertEquals(
                """
        Обновление!
        Заголовок: coolTitle
        Время обновления: 2023-02-01T12:00
        Имя пользователя coolUser
        Новый issue: someInfo
        """,
                issueInfo.toString());
    }
}
