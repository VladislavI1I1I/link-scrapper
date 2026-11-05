package backend.academy.bot.controller;

import backend.academy.bot.service.UpdateService;
import backend.academy.common.model.LinkUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {

    private final UpdateService updateService;

    public BotController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @PostMapping("/updates")
    public ResponseEntity<String> getUpdates(@RequestBody LinkUpdate update) {
        updateService.handleUpdate(update);
        return ResponseEntity.ok("Update received");
    }
}
