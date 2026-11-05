package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.storage.hibernate.HibernateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "database.type=orm")
public class HibernateServiceConditionalTest extends AbstractScrapperTest {

    @Test
    void loadHibernateDatabaseTest() {
        assertThat(databaseService).isInstanceOf(HibernateService.class);
    }
}
