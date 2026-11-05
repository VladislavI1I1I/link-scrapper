package backend.academy.scrapper.storage.hibernate;

import backend.academy.scrapper.configurations.PaginationConfig;
import backend.academy.scrapper.model.SubscriptionData;
import backend.academy.scrapper.model.UserLinkOptions;
import backend.academy.scrapper.storage.DatabaseService;
import backend.academy.scrapper.storage.hibernate.model.*;
import backend.academy.scrapper.storage.hibernate.repos.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(prefix = "database", name = "type", havingValue = "orm")
public class HibernateService implements DatabaseService {

    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;
    private final UserLinkRepository userLinkRepository;
    private final UserLinkTagRepository userLinkTagRepository;
    private final UserLinkFilterRepository userLinkFilterRepository;
    private final PaginationConfig paginationConfig;

    @Override
    @Transactional
    public void addUserById(Long id) {
        if (!userRepository.existsById(id)) {
            userRepository.save(new User(id));
            log.atInfo().log("Добавлен пользователь с id {}", id);
        } else {
            log.atDebug().log("Пользователь с id {} уже существует", id);
        }
    }

    @Override
    @Transactional
    public Boolean deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.atInfo().log("Удален пользователь с id {}", id);
            return true;
        } else {
            log.atWarn().log("Пользователь с id {} не найден.", id);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, UserLinkOptions> getUserLinksById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id: " + id + " не существует"));

        List<UserLink> userLinks = userLinkRepository.findByUser(user);

        Map<String, UserLinkOptions> result = new HashMap<>();
        for (UserLink userLink : userLinks) {
            Link link = userLink.link();
            if (link != null) {
                Optional<String> tagOpt = userLinkTagRepository.findTagByUserAndLink(user, link);
                List<String> filters = userLinkFilterRepository.findFiltersByUserAndLink(user, link);
                result.put(link.url(), new UserLinkOptions(tagOpt.orElse(null), filters));
            }
        }
        return result;
    }

    @Override
    public SubscriptionData getLinkSubscriptionData(String url) {
        Link link = linkRepository
                .findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException("Ссылка: " + url + " не найдена"));

        List<Long> subscriberIds = userLinkRepository.findSubscriberIdsByLink(link);

        return new SubscriptionData(link.id(), subscriberIds, link.updateTime());
    }

    @Override
    @Transactional
    public void setUserLinkUpdateTime(String url, LocalDateTime updateTime) {
        Link link = linkRepository
                .findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException("Ссылка: " + url + " не найдена"));
        link.updateTime(updateTime);
        linkRepository.save(link);
        log.atInfo().log("Обновлено время последнего обновления для ссылки: {}", url);
    }

    @Override
    public boolean checkExistence(String url) {
        return linkRepository.findByUrl(url).isPresent();
    }

    @Override
    @Transactional
    public void addUserLinkByData(
            Long userId, String url, String tagName, List<String> filterNames, LocalDateTime updateTime) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id: " + userId + " не существует"));

        Link link = linkRepository.findByUrl(url).orElseGet(() -> {
            log.atInfo().log("Creating new link: {}", url);
            return linkRepository.save(new Link(url, updateTime));
        });

        if (!userLinkRepository.existsByUserAndLink(user, link)) {
            userLinkRepository.save(new UserLink(user, link));
            log.atInfo().log("Cвязал пользователя {} с ссылкой {}", userId, url);
        }

        if (tagName != null && !tagName.isBlank()) {
            Tag tag = tagRepository.findByTag(tagName).orElseGet(() -> {
                log.atInfo().log("Создаю новый тег: {}", tagName);
                return tagRepository.save(new Tag(tagName));
            });

            if (!userLinkTagRepository.existsByUserAndLinkAndTag(user, link, tag)) {
                userLinkTagRepository.save(new UserLinkTag(link, user, tag));
                log.atInfo().log("Связал тег '{}' с пользователем {} и ссылкой {}", tagName, userId, url);
            }
        } else {
            log.atDebug().log("Тега для пользователя {} и ссылки {} не предоставлено", userId, url);
        }

        if (filterNames != null
                && !filterNames.isEmpty()
                && filterNames.stream().anyMatch(f -> f != null && !f.isBlank())) {
            for (String filterName : filterNames) {
                if (filterName == null || filterName.isBlank()) continue;

                Filter filter = filterRepository.findByFilter(filterName).orElseGet(() -> {
                    log.atInfo().log("Добавлен новый фильтр: {}", filterName);
                    return filterRepository.save(new Filter(filterName));
                });

                if (!userLinkFilterRepository.existsByUserAndLinkAndFilter(user, link, filter)) {
                    userLinkFilterRepository.save(new UserLinkFilter(link, user, filter));
                    log.atInfo().log("Связал фильтр '{}' с пользователем {} и ссылкой {}", filterName, userId, url);
                }
            }
        } else {
            log.atDebug().log("Фильтров для пользователя {} и ссылки {} не предоставлено", userId, url);
        }
    }

    @Override
    @Transactional
    public void removeUserLink(Long userId, String url) {
        log.atInfo().log("Попытка удаления ссылки:{} у пользователя: {}", url, userId);

        Optional<User> possibleUser = userRepository.findById(userId);
        Optional<Link> possibleLink = linkRepository.findByUrl(url);

        if (possibleUser.isPresent() && possibleLink.isPresent()) {

            int deletedCount =
                    userLinkRepository.deleteByUserAndLink(possibleUser.orElseThrow(), possibleLink.orElseThrow());
            if (deletedCount > 0) {
                log.atInfo().log("Удалена связь ссылки: {} с пользователем: {}", url, userId);
            } else {
                log.atWarn().log("Связь ссылки: {} с пользователем: {} не найдена", url, userId);
            }
        } else {
            log.atWarn().log("Пользователь или ссылка не были найдены. ID пользователя: {}, URL: {}", userId, url);
        }
        var subscribers = getLinkSubscriptionData(url).subscribers();
        if (subscribers.isEmpty()) {
            linkRepository.delete(possibleLink.orElseThrow());
        }
    }

    @Override
    public List<String> getLinks(Long lastId) {
        Pageable pageable =
                PageRequest.of(0, paginationConfig.batchSize(), Sort.by("id").ascending());

        List<String> linkList;
        if (lastId == null || lastId <= 0) {
            linkList = linkRepository.findAllUrls(pageable);
        } else {
            linkList = linkRepository.findUrlsByIdGreaterThan(lastId, pageable);
        }

        return linkList;
    }
}
