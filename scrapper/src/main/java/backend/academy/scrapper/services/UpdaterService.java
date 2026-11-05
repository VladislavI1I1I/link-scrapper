package backend.academy.scrapper.services;

import backend.academy.scrapper.LinkCheckerAndUpdater;
import backend.academy.scrapper.clients.bot.BotClientContract;
import backend.academy.scrapper.model.WebsiteUpdateInfo;
import backend.academy.scrapper.services.link.services.LinkService;
import backend.academy.scrapper.storage.DatabaseService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdaterService {
    private static final long UPDATE_CHECK_INTERVAL_MS = 10_000L;

    private final DatabaseService scrapperDatabaseService;
    private final BotClientContract botClient;
    private final LinkService linkService;
    private final LinkCheckerAndUpdater linkChecker;

    @Scheduled(fixedRate = UPDATE_CHECK_INTERVAL_MS)
    public void updateInfo() {
        log.atInfo().addKeyValue("action", "update_info").log("Обновляю базу данных...");

        long currentId = 0L;
        List<String> links = scrapperDatabaseService.getLinks(currentId);
        while (!links.isEmpty()) {
            for (String url : links) {
                currentId++;
                processLink(url);
            }
            links = scrapperDatabaseService.getLinks(currentId);
        }
    }

    private void processLink(String url) {
        Optional<WebsiteUpdateInfo> possibleUpdateInfo = linkChecker.checkAndGetUpdate(url);
        if (possibleUpdateInfo.isEmpty()) {
            logNoUpdates(url);
            return;
        }

        WebsiteUpdateInfo newUpdateInfo = possibleUpdateInfo.orElseThrow();
        LocalDateTime lastUpdateTime =
                scrapperDatabaseService.getLinkSubscriptionData(url).updateTime();
        if (lastUpdateTime != null && !newUpdateInfo.updateTime().isAfter(lastUpdateTime)) {
            logNoUpdates(url);
            return;
        }

        scrapperDatabaseService.setUserLinkUpdateTime(url, newUpdateInfo.updateTime());
        botClient.sendUpdate(linkService.getLinkUpdate(url, newUpdateInfo.toString()));
    }

    private void logNoUpdates(String url) {
        log.atInfo().log("Нет обновлений по ссылке:{}", url);
    }
}
