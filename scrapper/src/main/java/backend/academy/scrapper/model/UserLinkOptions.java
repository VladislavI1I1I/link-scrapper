package backend.academy.scrapper.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserLinkOptions {
    private String tag;
    private List<String> filters;
}
