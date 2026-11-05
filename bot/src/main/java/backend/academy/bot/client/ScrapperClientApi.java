package backend.academy.bot.client;

import backend.academy.common.model.AddLinkRequest;
import backend.academy.common.model.LinkResponse;
import backend.academy.common.model.ListLinksResponse;
import backend.academy.common.model.RemoveLinkRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface ScrapperClientApi {
    @PostExchange("/tg-chat/{chatId}")
    ResponseEntity<String> registerChat(@PathVariable("chatId") Long chatId);

    @DeleteExchange("/tg-chat/{chatId}")
    ResponseEntity<String> deleteChat(@PathVariable("chatId") Long chatId);

    @GetExchange("/links")
    ResponseEntity<ListLinksResponse> getAllTrackedLinks(@RequestHeader("Tg-Chat-Id") String chatId);

    @PostExchange("/links")
    ResponseEntity<LinkResponse> addTrackedLink(
            @RequestHeader("Tg-Chat-Id") String chatId, @RequestBody AddLinkRequest request);

    @DeleteExchange("/links")
    ResponseEntity<LinkResponse> removeTrackedLink(
            @RequestHeader("Tg-Chat-Id") String chatId, @RequestBody RemoveLinkRequest request);
}
