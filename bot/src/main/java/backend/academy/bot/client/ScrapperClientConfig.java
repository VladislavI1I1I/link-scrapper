package backend.academy.bot.client;

import backend.academy.bot.BotConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ScrapperClientConfig {

    private final BotConfig botConfig;

    public ScrapperClientConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory() {
        RestClient restClient =
                RestClient.builder().baseUrl(botConfig.baseUrl()).build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
    }

    @Bean
    public ScrapperClientApi scrapperClientApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ScrapperClientApi.class);
    }
}
