package backend.academy.scrapper.clients.update.checkers;

import backend.academy.scrapper.model.WebsiteUpdateInfo;
import java.util.Optional;

public interface UpdateCheckingClient {
    Optional<WebsiteUpdateInfo> checkUpdates(String link);
}
