package backend.academy.scrapper.services.link.services;

import backend.academy.common.exceptions.InvalidRequestArgumentsException;
import backend.academy.common.exceptions.LinkNotFoundException;
import backend.academy.common.model.AddLinkRequest;
import backend.academy.common.model.LinkResponse;
import backend.academy.common.model.LinkUpdate;
import backend.academy.common.model.ListLinksResponse;
import backend.academy.common.model.RemoveLinkRequest;
import backend.academy.scrapper.LinkCheckerAndUpdater;
import backend.academy.scrapper.model.UserLinkOptions;
import backend.academy.scrapper.model.WebsiteUpdateInfo;
import backend.academy.scrapper.storage.DatabaseService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LinkService {
    private final DatabaseService databaseService;
    private final LinkCheckerAndUpdater linkChecker;

    public LinkService(DatabaseService databaseService, LinkCheckerAndUpdater linkChecker) {
        this.databaseService = databaseService;
        this.linkChecker = linkChecker;
    }

    public LinkResponse getUserLinkResponseByIdAndUrl(Long id, String url) {
        UserLinkOptions userLinkOptions = databaseService.getUserLinksById(id).get(url);
        String tag = "";
        ArrayList<String> filters = new ArrayList<>();
        if (userLinkOptions.tag() != null) {
            tag = userLinkOptions.tag();
        }
        if (userLinkOptions.filters() != null) {
            filters.addAll(userLinkOptions.filters());
        }
        return new LinkResponse(databaseService.getLinkSubscriptionData(url).id(), url, tag, filters);
    }

    public LinkUpdate getLinkUpdate(String url, String message) {
        var linkData = databaseService.getLinkSubscriptionData(url);

        return new LinkUpdate(linkData.id(), url, message, linkData.subscribers());
    }

    public LinkResponse removeUserLink(Long id, String link) {
        if (isLinkAbsent(id, link)) {
            return null;
        }
        var response = getUserLinkResponseByIdAndUrl(id, link);
        databaseService.removeUserLink(id, link);

        return response;
    }

    public ListLinksResponse getLinks(Long id) {
        List<LinkResponse> links = new ArrayList<>();

        Map<String, UserLinkOptions> userLinksMap = databaseService.getUserLinksById(id);
        if (userLinksMap == null) {
            return new ListLinksResponse(new ArrayList<>(), 0);
        }

        for (Map.Entry<String, UserLinkOptions> entry : userLinksMap.entrySet()) {
            Long linkId =
                    databaseService.getLinkSubscriptionData(entry.getKey()).id();
            links.add(new LinkResponse(
                    linkId,
                    entry.getKey(),
                    entry.getValue().tag(),
                    entry.getValue().filters()));
        }
        return new ListLinksResponse(links, links.size());
    }

    public LinkResponse addUserLinkByData(Long id, String url, String tag, List<String> filters) {
        Optional<WebsiteUpdateInfo> updateInfo = linkChecker.checkAndGetUpdate(url);
        if (updateInfo.isEmpty()) {
            return null;
        }

        Map<String, UserLinkOptions> currentUserLinks = databaseService.getUserLinksById(id);
        if (currentUserLinks.containsKey(url)) {
            return null;
        }

        var newLinkData = new UserLinkOptions(tag, filters);
        currentUserLinks.put(url, newLinkData);

        databaseService.addUserLinkByData(
                id, url, tag, filters, updateInfo.orElseThrow().updateTime());

        return getUserLinkResponseByIdAndUrl(id, url);
    }

    public ResponseEntity<LinkResponse> addLink(Long id, AddLinkRequest request) {
        if (linkChecker.checkAndGetUpdate(request.link()).isEmpty()) {
            if (!databaseService.checkExistence(request.link())) {
                databaseService.addUserLinkByData(id, request.link(), request.tag(), request.filters(), null);
            }
        } else if (!isLinkAbsent(id, request.link())) {
            log.atWarn()
                    .log("Некорректные параметры запроса на добавление ссылки: chatId={}, link={}", id, request.link());
            throw new InvalidRequestArgumentsException("Некорректные параметры запроса");
        }
        log.atInfo().log("Ccылка успешно добавлена!");

        return ResponseEntity.ok(addUserLinkByData(id, request.link(), request.tag(), request.filters()));
    }

    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        log.atInfo().log("Удаляем ссылку {} в чате {}", request.link(), chatId);
        LinkResponse linkResponse = removeUserLink(chatId, request.link());
        if (linkResponse == null) {
            throw new LinkNotFoundException("Ссылка не найдена");
        }
        return ResponseEntity.ok(linkResponse);
    }

    private boolean isLinkAbsent(Long id, String link) {
        var links = databaseService.getUserLinksById(id);
        if (links == null) {
            return false;
        }
        return !links.containsKey(link);
    }
}
