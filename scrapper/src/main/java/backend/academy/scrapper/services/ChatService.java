package backend.academy.scrapper.services;

import backend.academy.common.exceptions.ChatNotFoundException;
import backend.academy.scrapper.storage.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {
    private final DatabaseService databaseService;

    public ChatService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public ResponseEntity<String> deleteChat(Long chatId) {

        if (!databaseService.deleteUserById(chatId)) {
            log.atError().log("Чата {} не существует", chatId);
            throw new ChatNotFoundException("Чат не существует");
        }
        log.atInfo().log("Чат удален!");
        return ResponseEntity.ok("Чат успешно удален");
    }

    public ResponseEntity<String> registerChat(Long chatId) {
        databaseService.addUserById(chatId);
        log.atInfo().log("Чат зарегистрирован!");
        return ResponseEntity.ok("Чат зарегистрирован");
    }
}
