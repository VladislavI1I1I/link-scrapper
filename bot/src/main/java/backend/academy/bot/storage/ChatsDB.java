package backend.academy.bot.storage;

import backend.academy.bot.model.ChatData;
import backend.academy.bot.states.State;
import backend.academy.common.model.AddLinkRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatsDB {
    private final Map<Long, ChatData> chatsData = new HashMap<>();

    public State getChatStateById(Long id) {
        return chatsData.get(id).currentChatState();
    }

    public AddLinkRequest chatIdToAddLinkRequest(Long id) {
        var chatData = chatsData.get(id);
        return new AddLinkRequest(chatData.bufferedLink(), chatData.bufferedTag(), chatData.bufferedFilters());
    }

    public void clearBufferById(Long id) {
        var chatData = chatsData.get(id);
        if (chatData.bufferedTag() != null) {
            chatData.bufferedTag("");
        }
        if (chatData.bufferedFilters() != null) {
            chatData.bufferedFilters().clear();
        }
        chatData.bufferedLink("");
    }

    public boolean contains(Long id) {
        return chatsData.containsKey(id);
    }

    public void setChatStateById(Long id, State state) {
        chatsData.get(id).currentChatState(state);
    }

    public void putNewUserById(Long id, State state) {
        chatsData.put(id, ChatData.builder().currentChatState(state).build());
    }

    public void deleteUserById(Long id) {
        chatsData.remove(id);
    }

    public void setLinkById(Long id, String link) {
        chatsData.get(id).bufferedLink(link);
    }

    public void setTagById(Long id, String tag) {
        chatsData.get(id).bufferedTag(tag);
    }

    public void setFiltersById(Long id, List<String> filters) {
        chatsData.get(id).bufferedFilters(filters);
    }

    public void setUserNameById(Long id, String username) {
        chatsData.computeIfPresent(id, (value, chatdata) -> ChatData.builder()
                .currentChatState(chatdata.currentChatState())
                .username(username)
                .build());
    }
}
