package backend.academy.scrapper.storage.jdbc;

import backend.academy.scrapper.configurations.PaginationConfig;
import backend.academy.scrapper.model.SubscriptionData;
import backend.academy.scrapper.model.UserLinkOptions;
import backend.academy.scrapper.storage.DatabaseService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(prefix = "database", name = "type", havingValue = "sql")
public class JdbcDatabaseService implements DatabaseService {
    private final JdbcClient jdbcClient;
    private final PaginationConfig paginationConfig;

    @Override
    @Transactional
    public void addUserById(Long id) {
        String sql = "INSERT INTO user_ids VALUES (?) ON CONFLICT (id) DO NOTHING";
        if (jdbcClient.sql(sql).param(id).update() > 0) {
            log.atInfo().log("Добавлен пользователь с id {}", id);
        }
    }

    @Override
    @Transactional
    public Boolean deleteUserById(Long id) {
        var deletedUserLinks = getUserLinksById(id);
        for (var url : deletedUserLinks.keySet()) {
            removeUserLink(id, url);
        }
        String sql = "DELETE FROM user_ids WHERE id = ?";
        var stringsAffected = jdbcClient.sql(sql).param(id).update();

        return stringsAffected > 0;
    }

    @Override
    public Map<String, UserLinkOptions> getUserLinksById(Long id) {
        String sql =
                """
                SELECT l.url, MIN(t.tag) AS tag, array_agg(DISTINCT f.filter) AS filters
                FROM links_users lu
                INNER JOIN links l ON lu.link_id = l.id
                LEFT JOIN links_tags lt ON lu.link_id = lt.link_id AND lu.user_id = lt.user_id
                LEFT JOIN tags t ON lt.tag_id = t.id
                LEFT JOIN links_filters lf ON lu.link_id = lf.link_id AND lu.user_id = lf.user_id
                LEFT JOIN filters f ON lf.filter_id = f.id
                WHERE lu.user_id = :userId
                GROUP BY l.url
                """;

        return jdbcClient
                .sql(sql)
                .param("userId", id)
                .query((rs, r) -> {
                    String link = rs.getString("url");
                    String tag = rs.getString("tag");
                    java.sql.Array sqlArray = rs.getArray("filters");
                    List<String> filters =
                            sqlArray != null ? Arrays.asList((String[]) sqlArray.getArray()) : Collections.emptyList();
                    UserLinkOptions options = new UserLinkOptions(tag, filters);
                    return Map.entry(link, options);
                })
                .list()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));
    }

    @Override
    public SubscriptionData getLinkSubscriptionData(String link) {
        String sql =
                """
                SELECT links.id, array_agg(user_id) as subscribers,links.update_time
                FROM links
                INNER JOIN links_users
                    ON links_users.link_id = links.id
                WHERE url = ?
                GROUP BY links.id
                """;
        return jdbcClient
                .sql(sql)
                .param(link)
                .query((rs, row_num) -> {
                    Long id = rs.getLong("id");
                    java.sql.Array sqlArray = rs.getArray("subscribers");
                    List<Long> subscribers =
                            sqlArray != null ? Arrays.asList((Long[]) sqlArray.getArray()) : Collections.emptyList();
                    LocalDateTime updateTime = rs.getObject("update_time", LocalDateTime.class);
                    return new SubscriptionData(id, subscribers, updateTime);
                })
                .single();
    }

    @Override
    @Transactional
    public void setUserLinkUpdateTime(String url, LocalDateTime updateTime) {
        String sql = "UPDATE links SET update_time = ? WHERE url = ?";
        jdbcClient.sql(sql).param(updateTime).param(url).query();
    }

    @Override
    public boolean checkExistence(String url) {
        String sql = """
            SELECT 1 FROM links WHERE url = ?
            """;
        return jdbcClient.sql(sql).query(Boolean.class).optional().orElse(false);
    }

    @Override
    @Transactional
    public void addUserLinkByData(
            Long userId, String link, String tag, List<String> filters, LocalDateTime updateTime) {
        String insertLink =
                """
                INSERT INTO links values (DEFAULT,:link, :time) ON CONFLICT (id) DO NOTHING ;
                """;
        jdbcClient.sql(insertLink).param("link", link).param("time", updateTime).update();

        String getLinkId = "select id from links where url = ?";
        Long linkId = jdbcClient.sql(getLinkId).param(link).query(Long.class).single();

        String insertLinkConnection = "INSERT INTO links_users VALUES (DEFAULT, ?, ?)";
        jdbcClient.sql(insertLinkConnection).param(userId).param(linkId).update();

        if (tag == null || tag.isEmpty()) {
            log.atWarn().log("тега нет");
            return;
        }
        String insertTag =
                """
                INSERT INTO tags values (DEFAULT,:tag);
                INSERT INTO links_tags values (DEFAULT,  :linkId, :userId, (SELECT id FROM tags WHERE tag = :tag));
                """;

        jdbcClient
                .sql(insertTag)
                .param("tag", tag)
                .param("linkId", linkId)
                .param("userId", userId)
                .update();

        if (filters == null || filters.isEmpty() || filters.getFirst().isEmpty()) {
            log.atWarn().log("фильтра нет");
            return;
        }
        String insertFilter =
                """
                INSERT INTO filters values (DEFAULT,:filter);
                INSERT INTO links_filters values (DEFAULT,:linkId, :userId, (SELECT id FROM filters WHERE filter = :filter));
                """;

        for (var filter : filters) {
            jdbcClient
                    .sql(insertFilter)
                    .param("filter", filter)
                    .param("linkId", linkId)
                    .param("userId", userId)
                    .update();
        }
    }

    @Override
    @Transactional
    public void removeUserLink(Long id, String link) {

        var subscribers = getLinkSubscriptionData(link).subscribers();
        var linkData = getUserLinksById(id).get(link);
        if (subscribers.size() == 1 && Objects.equals(subscribers.getFirst(), id)) {
            String removeLink = "DELETE FROM links where url = :url";
            jdbcClient.sql(removeLink).param("url", link).update();
            removeLinkFilters(link, linkData.filters());
            removeLinkTag(link, linkData.tag());
        }
        log.atInfo().log("Удаление ссылки:{} в чате: {}", link, id);
        String sql = "DELETE FROM links_users WHERE user_id = ? AND link_id = (select id from links where url = ?);";
        if (jdbcClient.sql(sql).param(id).param(link).update() == 0) {
            log.atWarn().log("Удаление ссылки:{} в чате: {} прошло безуспешно", link, id);
        }
    }

    @Override
    public List<String> getLinks(Long lastId) {
        String sql = "SELECT url FROM links WHERE id > ? ORDER BY id ASC LIMIT ?";
        List<String> linkList = jdbcClient
                .sql(sql)
                .param(lastId)
                .param(paginationConfig.batchSize())
                .query(String.class)
                .list();

        log.atInfo().log(
                "Получено {} ссылок (после id={}, размер={})",
                linkList.size(),
                lastId != null ? lastId : 0,
                paginationConfig);

        return linkList;
    }

    private void removeLinkFilters(String link, List<String> filters) {
        for (String filter : filters) {
            String removeConnection = "DELETE FROM links_filters "
                    + "WHERE link_id = (select id from links where url = ?) AND filter_id = (select id from filters where filter = ?);";
            jdbcClient.sql(removeConnection).param(link).param(filter).update();
            String removeFilterOptional = "DELETE FROM filters "
                    + "WHERE NOT EXISTS (SELECT 1 FROM links_filters WHERE links_filters.filter_id = filters.id)";
            jdbcClient.sql(removeFilterOptional).update();
        }
    }

    private void removeLinkTag(String link, String tag) {
        String removeConnection = "DELETE FROM links_tags "
                + "WHERE link_id = (select id from links where url = ?) AND tag_id = (select id from tags where tag = ?);";
        jdbcClient.sql(removeConnection).param(link).param(tag).update();
        String removeTagOptional =
                "DELETE FROM tags " + "WHERE NOT EXISTS (SELECT 1 FROM links_tags WHERE links_tags.tag_id = tags.id)";
        jdbcClient.sql(removeTagOptional).update();
    }
}
