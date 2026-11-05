package backend.academy.scrapper.clients.update.checkers;

import backend.academy.scrapper.configurations.ScrapperConfig;
import backend.academy.scrapper.model.WebsiteUpdateInfo;
import backend.academy.scrapper.model.github.GithubInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class GitHubClient implements UpdateCheckingClient {
    private static final int OWNER_INDEX = 1;
    private static final int REPO_INDEX = 2;

    private final String githubBaseUrl;
    private final String accessToken;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GitHubClient(ScrapperConfig scrapperConfig, RestClient restClient, ObjectMapper objectMapper) {
        githubBaseUrl = scrapperConfig.githubBaseUrl();
        accessToken = scrapperConfig.githubToken();
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<WebsiteUpdateInfo> checkUpdates(String link) {
        String repoUrl = convertLinkToApi(link, "{0}/{1}/{2}");
        String issuesUrl = convertLinkToApi(link, "{0}/{1}/{2}/issues");
        String prUrl = convertLinkToApi(link, "{0}/{1}/{2}/pulls");
        log.atInfo()
                .addKeyValue("action", "check_updates")
                .addKeyValue("url", repoUrl)
                .log("Проверка на наличие обновлений");

        ResponseEntity<String> issuesResponse = getResponse(issuesUrl);
        ResponseEntity<String> pullsResponse = getResponse(prUrl);

        try {
            GithubInfo issue = getGithubInfo(issuesResponse);
            GithubInfo pullRequest = getGithubInfo(pullsResponse);

            if (issue == null && pullRequest == null) {
                return Optional.empty();
            }

            if (pullRequest == null || (issue != null && issue.updateTime().isAfter(pullRequest.updateTime()))) {
                logSuccess("check_issues", issuesUrl, issue.updateTime().toLocalDateTime());
                return Optional.of(new WebsiteUpdateInfo(issue, "issue"));
            }
            if (issue == null || pullRequest.updateTime().isAfter(issue.updateTime())) {
                logSuccess(
                        "check_pull_requests", prUrl, pullRequest.updateTime().toLocalDateTime());
                return Optional.of(new WebsiteUpdateInfo(pullRequest, "pull request"));
            }

            return Optional.empty();
        } catch (JsonProcessingException e) {
            logError(repoUrl);
            return Optional.empty();
        } catch (NoSuchElementException e) {
            log.atWarn()
                    .addKeyValue("action", "check_updates")
                    .addKeyValue("url", repoUrl)
                    .log("У ссылки нет ");
            return Optional.empty();
        }
    }

    private void logError(String url) {
        log.atError()
                .addKeyValue("action", "check_updates")
                .addKeyValue("url", url)
                .log("Ошибка при разборе ответа от GitHub");
    }

    private void logSuccess(String action, String url, LocalDateTime updateTime) {
        log.atInfo()
                .addKeyValue("action", action)
                .addKeyValue("url", url)
                .addKeyValue("last_update", updateTime)
                .log("Последнее обновление репозитория получено");
    }

    private GithubInfo getGithubInfo(ResponseEntity<String> rawResponse) throws JsonProcessingException {
        List<GithubInfo> specialInfo = objectMapper.readValue(
                rawResponse.getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, GithubInfo.class));
        if (specialInfo == null || specialInfo.isEmpty()) {
            return null;
        }
        return specialInfo.getFirst();
    }

    private String convertLinkToApi(String link, String pattern) {
        Path path = Path.of(link);
        String convertedLink =
                MessageFormat.format(pattern, githubBaseUrl, path.getName(OWNER_INDEX), path.getName(REPO_INDEX));
        log.atInfo().log("Cконвертированная ссылка: {} ", convertedLink);
        return convertedLink;
    }

    private ResponseEntity<String> getResponse(String link) {
        if (accessToken.isEmpty()) {
            return restClient.get().uri(link).retrieve().toEntity(String.class);
        }
        return restClient
                .get()
                .uri(link)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toEntity(String.class);
    }
}
