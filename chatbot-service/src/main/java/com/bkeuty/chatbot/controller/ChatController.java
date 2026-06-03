package com.bkeuty.chatbot.controller;

import com.bkeuty.chatbot.dto.ChatRequest;
import com.bkeuty.chatbot.dto.ChatResponse;
import com.bkeuty.chatbot.entity.ChatMessage;
import com.bkeuty.chatbot.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/healthchecks")
    public ResponseEntity<String> healthchecks() {
        String status = chatService.checkHealth();
        if (status.contains("DOWN")) {
            return ResponseEntity.status(503).body(status);
        }
        return ResponseEntity.ok(status);
    }
    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthcheck() {

        return ResponseEntity.ok("OK");
    }
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.processChatMessage(request));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable(name = "sessionId") String sessionId) {
        return ResponseEntity.ok(chatService.getChatHistory(sessionId));
    }
}
