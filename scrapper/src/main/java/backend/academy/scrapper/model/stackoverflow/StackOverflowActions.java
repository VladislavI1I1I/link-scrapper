package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowActions(List<ActionItem> items) {}
