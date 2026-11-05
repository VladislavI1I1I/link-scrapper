package backend.academy.scrapper.configurations;

import backend.academy.scrapper.clients.bot.BotClientApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "http")
public class BotClientConfig {

    private final ScrapperConfig config;

    public BotClientConfig(ScrapperConfig config) {
        this.config = config;
    }

    @Bean
    public BotClientApi botClientApi() {
        RestClient restClient =
                RestClient.builder().baseUrl(config.clientBaseUrl()).build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(BotClientApi.class);
    }
}
