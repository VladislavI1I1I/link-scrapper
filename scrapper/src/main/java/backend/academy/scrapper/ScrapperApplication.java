package backend.academy.scrapper;

import backend.academy.common.model.TopicsInfo;
import backend.academy.scrapper.configurations.DataSourceConfig;
import backend.academy.scrapper.configurations.PaginationConfig;
import backend.academy.scrapper.configurations.ScrapperConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableKafka
@EnableConfigurationProperties({ScrapperConfig.class, DataSourceConfig.class, PaginationConfig.class, TopicsInfo.class})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
