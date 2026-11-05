package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubInfo(
        String title, String body, GitHubUser user, @JsonProperty("updated_at") ZonedDateTime updateTime) {}
