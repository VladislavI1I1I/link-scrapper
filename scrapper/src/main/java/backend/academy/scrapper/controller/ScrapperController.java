package backend.academy.scrapper.controller;

import backend.academy.common.model.AddLinkRequest;
import backend.academy.common.model.LinkResponse;
import backend.academy.common.model.ListLinksResponse;
import backend.academy.common.model.RemoveLinkRequest;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.link.services.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class ScrapperController {
    private final LinkService linkService;
    private final ChatService chatService;

    public ScrapperController(LinkService linkService, ChatService chatService) {
        this.linkService = linkService;
        this.chatService = chatService;
    }

    @PostMapping("/tg-chat/{chatId}")
    public ResponseEntity<String> registerChat(@PathVariable Long chatId) {
        return chatService.registerChat(chatId);
    }

    @DeleteMapping("/tg-chat/{chatId}")
    public ResponseEntity<String> deleteChat(@PathVariable Long chatId) {
        return chatService.deleteChat(chatId);
    }

    @GetMapping("/links")
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        return ResponseEntity.ok(linkService.getLinks(chatId));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        return linkService.removeLink(chatId, request);
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long id, @RequestBody AddLinkRequest request) {

        return linkService.addLink(id, request);
    }
}
