package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.storage.jdbc.JdbcDatabaseService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "database.type=sql")
public class JdbcDatabaseServiceConditionalTest extends AbstractScrapperTest {

    @Test
    void loadJdbcDatabaseTest() {
        assertThat(databaseService).isInstanceOf(JdbcDatabaseService.class);
    }
}
