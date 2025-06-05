package com.avs.conversia.chat_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avs.conversia.chat_service.dto.ConversaDTO;
import com.avs.conversia.chat_service.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{botId}")
    public ResponseEntity<ConversaDTO> interagir(
            @PathVariable Long botId,
            @RequestParam Long tenantId,
            @RequestBody String mensagem) {
        return ResponseEntity.ok(chatService.interagir(botId, tenantId, mensagem));
    }

    @GetMapping("/historico/{botId}")
    public ResponseEntity<List<ConversaDTO>> listarHistorico(
            @PathVariable Long botId,
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(chatService.listarHistorico(botId, tenantId));
    }
}