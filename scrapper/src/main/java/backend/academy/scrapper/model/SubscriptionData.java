package backend.academy.scrapper.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubscriptionData {
    Long id;
    List<Long> subscribers;
    LocalDateTime updateTime;
}
