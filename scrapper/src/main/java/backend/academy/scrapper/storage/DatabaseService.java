package backend.academy.scrapper.storage;

import backend.academy.scrapper.model.SubscriptionData;
import backend.academy.scrapper.model.UserLinkOptions;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DatabaseService {
    void addUserById(Long id);

    Boolean deleteUserById(Long id);

    Map<String, UserLinkOptions> getUserLinksById(Long id);

    SubscriptionData getLinkSubscriptionData(String link);

    void setUserLinkUpdateTime(String url, LocalDateTime updateTime);

    boolean checkExistence(String url);

    void addUserLinkByData(Long id, String link, String tag, List<String> filters, LocalDateTime updateTime);

    void removeUserLink(Long id, String link);

    List<String> getLinks(Long lastId);
}
