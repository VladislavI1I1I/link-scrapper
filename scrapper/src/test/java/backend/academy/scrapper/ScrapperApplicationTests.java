package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.common.exceptions.InvalidRequestArgumentsException;
import backend.academy.common.model.AddLinkRequest;
import backend.academy.common.model.RemoveLinkRequest;
import backend.academy.scrapper.clients.update.checkers.GitHubClient;
import backend.academy.scrapper.clients.update.checkers.StackOverflowClient;
import backend.academy.scrapper.configurations.DataSourceConfig;
import backend.academy.scrapper.controller.ScrapperController;
import backend.academy.scrapper.model.WebsiteUpdateInfo;
import backend.academy.scrapper.util.MigrationsRunner;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class ScrapperApplicationTests extends AbstractScrapperTest {
    String githubTestUrl = "https://github.com/enhorse/java-interview";

    @Autowired
    private ScrapperController scrapperController;

    @Autowired
    private DataSourceConfig dataSource;

    @Autowired
    private GitHubClient gitHubClient;

    @MockitoBean
    private final LinkCheckerAndUpdater linkCheckerMock = mock(LinkCheckerAndUpdater.class);

    @Autowired
    private StackOverflowClient stackOverflowClient;

    @Autowired
    MigrationsRunner migrationsRunner;

    private static WireMockServer wireMockServer;

    private static WebsiteUpdateInfo oldInfo;

    private void resetDatabase() {
        try (Connection connection =
                DriverManager.getConnection(dataSource.url(), dataSource.username(), dataSource.password())) {
            log.atError().log(dataSource.url());
            Statement stmt = connection.createStatement();
            stmt.execute("DROP SCHEMA public CASCADE");
            stmt.execute("CREATE SCHEMA public");
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset DB schema", e);
        }
        migrationsRunner.runMigrations();
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        wireMockServer.stubFor(get(urlPathMatching("/enhorse/java-interview"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"updated_at\": \"2020-11-11T18:12:00Z\"}")));

        LocalDateTime oldTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        oldInfo = new WebsiteUpdateInfo(" ", " ", " ", oldTime, " ");
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        resetDatabase();
        when(linkCheckerMock.checkAndGetUpdate(githubTestUrl)).thenReturn(Optional.of(oldInfo));
        databaseService.addUserById(12345L);
    }

    @Test
    void AddLinkToDatabaseTest() {

        List<String> filters = new ArrayList<>();
        filters.add("cool filter");
        AddLinkRequest requestMock = new AddLinkRequest(githubTestUrl, "cool tag", filters);
        var response = scrapperController.addLink(12345L, requestMock).getBody();
        assertEquals("cool filter", response.filters().getFirst());
        assertEquals("cool tag", response.tag());
        assertEquals("https://github.com/enhorse/java-interview", response.url());
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(githubTestUrl);
        scrapperController.removeLink(12345L, removeLinkRequest).getBody();
        scrapperController.deleteChat(12345L);
    }

    @Test
    void AddLinkToDatabaseAndRemoveTest() {

        List<String> filters = new ArrayList<>();
        filters.add("cool filter");
        AddLinkRequest requestMock = new AddLinkRequest(githubTestUrl, "cool tag", filters);
        var addResponse = scrapperController.addLink(12345L, requestMock).getBody();

        assertEquals("cool filter", addResponse.filters().getFirst());
        assertEquals("cool tag", addResponse.tag());
        assertEquals("https://github.com/enhorse/java-interview", addResponse.url());

        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(githubTestUrl);
        var removeResponse =
                scrapperController.removeLink(12345L, removeLinkRequest).getBody();
        assertEquals("cool filter", removeResponse.filters().getFirst());
        assertEquals("cool tag", removeResponse.tag());
        assertEquals("https://github.com/enhorse/java-interview", removeResponse.url());
    }

    @Test
    void doubleAddErrorTest() {

        List<String> filters = new ArrayList<>();
        filters.add("cool filter");
        AddLinkRequest requestMock = new AddLinkRequest(githubTestUrl, "cool tag", filters);
        scrapperController.addLink(12345L, requestMock).getBody();
        Assert.assertThrows(
                InvalidRequestArgumentsException.class, () -> scrapperController.addLink(12345L, requestMock));
    }

    @Test
    void clientErrorsTest() {

        wireMockServer.stubFor(get(urlPathMatching("/questions/74364929"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"items\": [{\"last_activity_date\": 1605118320}]}")));

        wireMockServer.stubFor(get(urlPathMatching("/abracadabra/not-valid/issues"))
                .willReturn(aResponse().withStatus(400)));
        wireMockServer.stubFor(get(urlPathMatching("/abracadabra/not-valid/pulls"))
                .willReturn(aResponse().withStatus(400)));
        wireMockServer.stubFor(get(urlPathMatching("/abracadabra/not-valid"))
                .willReturn(aResponse().withStatus(400)));

        wireMockServer.stubFor(
                get(urlPathMatching("/questions/123457487864\\?order=desc&sort=activity&site=stackoverflow"))
                        .willReturn(aResponse().withStatus(404)));

        String invalidSOUrl = "stackoverflow.com/questions/123457487864";
        String invalidGithubUrl = "github.com/abracadabra/not-valid";

        Assert.assertThrows(
                HttpClientErrorException.BadRequest.class, () -> gitHubClient.checkUpdates(invalidGithubUrl));
        Assert.assertThrows(
                HttpClientErrorException.NotFound.class, () -> stackOverflowClient.checkUpdates(invalidSOUrl));
    }
}
