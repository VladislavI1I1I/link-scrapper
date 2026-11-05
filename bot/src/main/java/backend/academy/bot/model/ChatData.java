package backend.academy.bot.model;

import backend.academy.bot.states.State;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatData {
    private String username;
    private State currentChatState;
    private String bufferedLink;
    private String bufferedTag;
    private List<String> bufferedFilters;
}
