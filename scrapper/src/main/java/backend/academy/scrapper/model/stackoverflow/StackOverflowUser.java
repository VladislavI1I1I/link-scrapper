package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowUser(@JsonProperty("display_name") String displayName) {}
