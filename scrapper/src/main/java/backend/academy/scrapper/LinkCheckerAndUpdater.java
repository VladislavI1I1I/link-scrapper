package backend.academy.scrapper;

import static backend.academy.scrapper.util.LinkFormatter.normalizeLink;

import backend.academy.scrapper.clients.update.checkers.GitHubClient;
import backend.academy.scrapper.clients.update.checkers.StackOverflowClient;
import backend.academy.scrapper.clients.update.checkers.UpdateCheckingClient;
import backend.academy.scrapper.model.WebsiteUpdateInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkCheckerAndUpdater {

    private final Map<String, UpdateCheckingClient> clientsProvider = new HashMap<>();

    public LinkCheckerAndUpdater(GitHubClient gitHubClient, StackOverflowClient stackOverflowClient) {
        clientsProvider.put("github.com/", gitHubClient);
        clientsProvider.put("stackoverflow.com/", stackOverflowClient);
    }

    public Optional<WebsiteUpdateInfo> checkAndGetUpdate(String link) {
        for (var client : clientsProvider.entrySet()) {
            link = normalizeLink(link);

            log.atInfo().log("Проверка ссылки: {}", link);
            if (link.startsWith(client.getKey())) {
                var updateInfo = client.getValue().checkUpdates(link);
                if (updateInfo.isPresent()) {
                    log.atInfo().log(
                            "Время обновления: {}", updateInfo.orElseThrow().updateTime());
                    return updateInfo;
                }
            }
        }
        return Optional.empty();
    }
}
