package backend.academy.bot.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.AbstractBotIntegrationTest;
import backend.academy.bot.RedisConfig;
import backend.academy.bot.client.ScrapperClient;
import backend.academy.common.model.AddLinkRequest;
import backend.academy.common.model.LinkResponse;
import backend.academy.common.model.ListLinksResponse;
import backend.academy.common.model.RemoveLinkRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;

class TrackedLinksCacheIntegrationTest extends AbstractBotIntegrationTest {
    private static final long CHAT_ID = 42L;
    private static final String CHAT_ID_HEADER = String.valueOf(CHAT_ID);
    private static final String LINK = "https://github.com/enhorse/java-interview";

    @Autowired
    private ScrapperClient scrapperClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(RedisConfig.LIST_CACHE).clear();
        when(scrapperClientApi.getAllTrackedLinks(CHAT_ID_HEADER)).thenReturn(ResponseEntity.ok(trackedLinks()));
    }

    private static ListLinksResponse trackedLinks() {
        var link = new LinkResponse(1L, LINK, "interview", new ArrayList<>(List.of("user=vlad")));
        return new ListLinksResponse(new ArrayList<>(List.of(link)), 1);
    }

    @Test
    void repeatedReadShouldBeServedFromCache() {
        ListLinksResponse first = scrapperClient.getAllTrackedLinks(CHAT_ID);
        ListLinksResponse second = scrapperClient.getAllTrackedLinks(CHAT_ID);

        assertThat(second).isEqualTo(first);
        verify(scrapperClientApi, times(1)).getAllTrackedLinks(CHAT_ID_HEADER);
    }

    @Test
    void addingLinkShouldInvalidateCache() {
        when(scrapperClientApi.addTrackedLink(anyString(), any(AddLinkRequest.class)))
                .thenReturn(ResponseEntity.ok(trackedLinks().links().getFirst()));

        scrapperClient.getAllTrackedLinks(CHAT_ID);
        scrapperClient.addTrackedLink(CHAT_ID, new AddLinkRequest(LINK, "interview", List.of()));
        scrapperClient.getAllTrackedLinks(CHAT_ID);

        verify(scrapperClientApi, times(2)).getAllTrackedLinks(CHAT_ID_HEADER);
    }

    @Test
    void removingLinkShouldInvalidateCache() {
        when(scrapperClientApi.removeTrackedLink(anyString(), any(RemoveLinkRequest.class)))
                .thenReturn(ResponseEntity.ok(trackedLinks().links().getFirst()));

        scrapperClient.getAllTrackedLinks(CHAT_ID);
        scrapperClient.removeTrackedLink(CHAT_ID, new RemoveLinkRequest(LINK));
        scrapperClient.getAllTrackedLinks(CHAT_ID);

        verify(scrapperClientApi, times(2)).getAllTrackedLinks(CHAT_ID_HEADER);
    }

    @Test
    void deletingChatShouldInvalidateCache() {
        when(scrapperClientApi.deleteChat(CHAT_ID)).thenReturn(ResponseEntity.ok("Чат успешно удален"));

        scrapperClient.getAllTrackedLinks(CHAT_ID);
        scrapperClient.deleteChat(CHAT_ID);
        scrapperClient.getAllTrackedLinks(CHAT_ID);

        verify(scrapperClientApi, times(2)).getAllTrackedLinks(CHAT_ID_HEADER);
    }
}
