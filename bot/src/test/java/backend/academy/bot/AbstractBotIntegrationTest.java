package backend.academy.bot;

import backend.academy.bot.client.ScrapperClientApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "app.telegram-token=test-token")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractBotIntegrationTest {

    @MockitoBean
    protected LinkTrackerBot bot;

    @MockitoBean
    protected ScrapperClientApi scrapperClientApi;
}
