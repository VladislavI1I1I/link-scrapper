package backend.academy.common.model;

import java.util.List;

public record LinkResponse(Long id, String url, String tag, List<String> filters) {}
