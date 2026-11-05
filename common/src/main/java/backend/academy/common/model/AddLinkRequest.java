package backend.academy.common.model;

import java.util.List;

public record AddLinkRequest(String link, String tag, List<String> filters) {}
