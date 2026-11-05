package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActionItem(
        @JsonProperty("last_activity_date") long lastActivityDate,
        @JsonProperty("body_markdown") String body,
        StackOverflowUser owner,
        String title) {}
