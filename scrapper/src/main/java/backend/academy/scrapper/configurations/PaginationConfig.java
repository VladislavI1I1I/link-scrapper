package backend.academy.scrapper.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(value = "pagination")
public record PaginationConfig(int batchSize) {}
