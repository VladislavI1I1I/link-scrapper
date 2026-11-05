package backend.academy.scrapper.util;

import backend.academy.scrapper.configurations.DataSourceConfig;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "migrations", havingValue = "run")
public class MigrationsRunner {
    private final DataSourceConfig dataSource;

    @PostConstruct
    public void runMigrations() {
        try (Connection connection =
                DriverManager.getConnection(dataSource.url(), dataSource.username(), dataSource.password())) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase =
                    new Liquibase("migrations/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database);

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            log.error("Произошла ошибка во время применения миграций: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
