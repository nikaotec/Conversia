package com.avs.conversia.chat_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avs.conversia.chat_service.dto.ChatRequestDTO;
import com.avs.conversia.chat_service.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{botId}")
    public ResponseEntity<String> interagirComBot(@PathVariable Long botId, @Valid @RequestBody ChatRequestDTO request) {
        String resposta = chatService.interagirComBot(botId, request);
        return ResponseEntity.ok(resposta);
    }
}
