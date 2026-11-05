package backend.academy.scrapper.configurations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = "spring.datasource")
public record DataSourceConfig(@NotEmpty String url, @NotEmpty String username, @NotEmpty String password) {}
