package backend.academy.common.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ListLinksResponse(
        @NotEmpty(message = "Прежде чем запросить список ссылок, начните отслеживать ссылки") List<LinkResponse> links,
        int size) {}
