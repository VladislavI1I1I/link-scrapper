package backend.academy.bot.client;

import backend.academy.bot.RedisConfig;
import backend.academy.common.model.AddLinkRequest;
import backend.academy.common.model.LinkResponse;
import backend.academy.common.model.ListLinksResponse;
import backend.academy.common.model.RemoveLinkRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Service
@AllArgsConstructor
public class ScrapperClient {

    private final ScrapperClientApi scrapperClientApi;

    public String registerChat(Long chatId) {
        log.atInfo()
                .addKeyValue("action", "register_chat")
                .addKeyValue("chatId", chatId)
                .log("Регистрация чата");
        try {
            ResponseEntity<String> response = scrapperClientApi.registerChat(chatId);
            log.atInfo().addKeyValue("response", response).log("Ответ от сервера на регистрацию чата");
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.atError()
                    .addKeyValue("action", "register_chat")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("status", e.getStatusCode())
                    .addKeyValue("error", e.getResponseBodyAsString())
                    .log("Ошибка при регистрации чата");
            throw e;
        }
    }

    @CacheEvict(value = RedisConfig.LIST_CACHE, key = "#chatId")
    public String deleteChat(Long chatId) {
        log.atInfo()
                .addKeyValue("action", "delete_chat")
                .addKeyValue("chatId", chatId)
                .log("Удаление чата");
        return scrapperClientApi.deleteChat(chatId).getBody();
    }

    @Cacheable(value = RedisConfig.LIST_CACHE, key = "#chatId")
    public ListLinksResponse getAllTrackedLinks(Long chatId) {
        log.atInfo()
                .addKeyValue("action", "get_all_links")
                .addKeyValue("chatId", chatId)
                .log("Получение всех отслеживаемых ссылок");
        return scrapperClientApi.getAllTrackedLinks(String.valueOf(chatId)).getBody();
    }

    @CacheEvict(value = RedisConfig.LIST_CACHE, key = "#chatId")
    public LinkResponse addTrackedLink(Long chatId, AddLinkRequest request) {
        log.atInfo()
                .addKeyValue("action", "add_tracked_link")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", request.link())
                .log("Добавление отслеживаемой ссылки");
        try {
            ResponseEntity<LinkResponse> response = scrapperClientApi.addTrackedLink(String.valueOf(chatId), request);
            log.atInfo().addKeyValue("response", response).log("Ответ от сервера при добавлении ссылки");
            return response.getBody();
        } catch (HttpServerErrorException e) {
            log.atError()
                    .addKeyValue("action", "add_tracked_link")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("status", e.getStatusCode())
                    .addKeyValue("error", e.getResponseBodyAsString())
                    .log("Ошибка при добавлении ссылки");
            throw e;
        }
    }

    @CacheEvict(value = RedisConfig.LIST_CACHE, key = "#chatId")
    public LinkResponse removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        log.atInfo()
                .addKeyValue("action", "remove_tracked_link")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", request.link())
                .log("Удаление отслеживаемой ссылки");
        try {
            ResponseEntity<LinkResponse> response =
                    scrapperClientApi.removeTrackedLink(String.valueOf(chatId), request);
            log.atInfo().addKeyValue("response", response).log("Ответ от сервера при удалении ссылки");
            return response.getBody();
        } catch (HttpServerErrorException e) {
            log.atError()
                    .addKeyValue("action", "remove_tracked_link")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("status", e.getStatusCode())
                    .addKeyValue("error", e.getResponseBodyAsString())
                    .log("Ошибка при удалении ссылки");
            throw e;
        }
    }
}
