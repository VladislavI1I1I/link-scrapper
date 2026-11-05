package backend.academy.scrapper.model;

import backend.academy.scrapper.model.github.GithubInfo;
import backend.academy.scrapper.model.stackoverflow.ActionItem;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record WebsiteUpdateInfo(String title, String body, String login, LocalDateTime updateTime, String infoType) {
    private static final int MAX_BODY_LENGTH = 200;

    public WebsiteUpdateInfo(GithubInfo githubInfo, String infoType) {
        this(
                githubInfo.title(),
                trimBody(githubInfo.body()),
                githubInfo.user().login(),
                githubInfo.updateTime().toLocalDateTime(),
                infoType);
    }

    public WebsiteUpdateInfo(ActionItem actionItem, ActionItem questionItem, String infoType) {
        this(
                questionItem.title(),
                trimBody(actionItem.body()),
                actionItem.owner().displayName(),
                LocalDateTime.ofEpochSecond(actionItem.lastActivityDate(), 0, ZoneOffset.UTC),
                infoType);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                """
            Обновление!
            Заголовок: {0}
            Время обновления: {1}
            Имя пользователя {2}
            Новый {3}: {4}
            """,
                title, updateTime, login, infoType, body);
    }

    private static String trimBody(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > MAX_BODY_LENGTH ? body.substring(0, MAX_BODY_LENGTH) : body;
    }
}
