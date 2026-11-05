package backend.academy.scrapper.clients.update.checkers;

import backend.academy.scrapper.configurations.ScrapperConfig;
import backend.academy.scrapper.model.WebsiteUpdateInfo;
import backend.academy.scrapper.model.stackoverflow.ActionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowActions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class StackOverflowClient implements UpdateCheckingClient {

    private static final String QUERY_SETTINGS = "?order=desc&sort=activity&site=stackoverflow";
    private static final String QUERY_SETTINGS_FOR_COMMENTS = "?order=desc&sort=creation&site=stackoverflow";
    private static final int INDEX_OF_QUESTION_ID = 2;

    private final String soBaseUrl;
    private final String accessToken;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public StackOverflowClient(ScrapperConfig scrapperConfig, RestClient restClient, ObjectMapper objectMapper) {
        soBaseUrl = scrapperConfig.stackOverflow().url();
        accessToken = scrapperConfig.stackOverflow().accessToken();
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<WebsiteUpdateInfo> checkUpdates(String link) {
        String questionLink = convertLinkToApi(link, "{0}/{1}{2}", QUERY_SETTINGS);
        String answerLink = convertLinkToApi(link, "{0}/{1}/answers{2}&filter=!nNPvSNe7Gv", QUERY_SETTINGS);
        String commentLink =
                convertLinkToApi(link, "{0}/{1}/comments{2}&filter=!nNPvSN_LI9", QUERY_SETTINGS_FOR_COMMENTS);
        log.atInfo()
                .addKeyValue("action", "check_updates")
                .addKeyValue("url", link)
                .log("Checking for updates... {}", questionLink);

        String questionResponse = getResponse(questionLink).getBody();
        String answerResponse = getResponse(answerLink).getBody();
        String commentResponse = getResponse(commentLink).getBody();

        try {
            ActionItem question = parseAction(questionResponse);
            ActionItem answer = parseAction(answerResponse);
            ActionItem comment = parseAction(commentResponse);

            if (question == null) {
                return Optional.empty();
            }
            if (answer != null && question.lastActivityDate() == answer.lastActivityDate()) {
                return Optional.of(new WebsiteUpdateInfo(answer, question, "Ответ"));
            }
            if (comment != null && question.lastActivityDate() == comment.lastActivityDate()) {
                return Optional.of(new WebsiteUpdateInfo(comment, question, "Комментарий"));
            }
        } catch (JsonProcessingException e) {
            log.atError().log("Ошибка при обработке JSON-ответа StackOverflow", e);
        }
        return Optional.empty();
    }

    private ActionItem parseAction(String body) throws JsonProcessingException {
        var parsed = objectMapper.readValue(body, StackOverflowActions.class);
        if (parsed == null || parsed.items() == null || parsed.items().isEmpty()) {
            return null;
        }
        return parsed.items().getFirst();
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

    private String convertLinkToApi(String link, String pattern, String querySettings) {
        Path path = Path.of(link);

        return MessageFormat.format(pattern, soBaseUrl, path.getName(INDEX_OF_QUESTION_ID), querySettings);
    }
}
