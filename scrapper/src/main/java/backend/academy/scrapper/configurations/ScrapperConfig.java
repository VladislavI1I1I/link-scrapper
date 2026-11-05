package backend.academy.scrapper.configurations;

import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
@EnableTransactionManagement
@Slf4j
public record ScrapperConfig(
        @NotEmpty String githubToken,
        @NotEmpty String githubBaseUrl,
        @NotEmpty String clientBaseUrl,
        StackOverflowCredentials stackOverflow) {

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken, @NotEmpty String url) {}
}
