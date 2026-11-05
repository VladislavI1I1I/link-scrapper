package backend.academy.scrapper.storage;

import backend.academy.scrapper.model.SubscriptionData;
import backend.academy.scrapper.model.UserLinkOptions;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryDatabase implements DatabaseService {

    private final AtomicLong idValue = new AtomicLong(0);

    private final Map<Long, Map<String, UserLinkOptions>> usersLinks = new HashMap<>();

    private final Map<String, SubscriptionData> linksInfo = new HashMap<>();

    @Override
    public void addUserById(Long id) {
        usersLinks.put(id, new HashMap<>());
    }

    @Override
    public Boolean deleteUserById(Long id) {
        if (!usersLinks.containsKey(id)) {
            return false;
        }

        var deletableUserLinks = usersLinks.get(id).keySet();
        for (var userLink : deletableUserLinks) {
            linksInfo.get(userLink).subscribers().remove(id);
        }

        return true;
    }

    @Override
    public Map<String, UserLinkOptions> getUserLinksById(Long id) {
        return usersLinks.get(id);
    }

    @Override
    public SubscriptionData getLinkSubscriptionData(String link) {
        return linksInfo.get(link);
    }

    @Override
    public void setUserLinkUpdateTime(String url, LocalDateTime newUpdateTime) {
        linksInfo.get(url).updateTime(newUpdateTime);
    }

    @Override
    public boolean checkExistence(String url) {
        return linksInfo.containsKey(url);
    }

    @Override
    public void addUserLinkByData(Long id, String url, String tag, List<String> filters, LocalDateTime updateTime) {
        if (linksInfo.containsKey(url)) {
            linksInfo.get(url).subscribers().add(id);
        } else {
            linksInfo.put(url, new SubscriptionData(idValue.getAndIncrement(), new ArrayList<>(), updateTime));
            linksInfo.get(url).subscribers().add(id);
        }
    }

    @Override
    public void removeUserLink(Long id, String link) {
        linksInfo.get(link).subscribers().remove(id);
        if (linksInfo.get(link).subscribers().isEmpty()) {
            linksInfo.remove(link);
        }
        usersLinks.get(id).remove(link);
    }

    @Override
    public List<String> getLinks(Long lastId) {
        return linksInfo.keySet().stream().toList();
    }
}
