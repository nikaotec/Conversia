package com.avs.conversia.bot_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avs.conversia.bot_service.dto.BotDTO;
import com.avs.conversia.bot_service.entity.Bot;
import com.avs.conversia.bot_service.service.BotService;
import com.avs.conversia.chat_service.entity.ChatMessage;
import com.avs.conversia.chat_service.repository.ChatMessageRepository;

@RestController
@RequestMapping("/api/bots")
public class BotController {
    private final BotService botService;
    private final ChatMessageRepository chatMessageRepository;

    public BotController(BotService botService, ChatMessageRepository chatMessageRepository) {
        this.botService = botService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @PostMapping
    public ResponseEntity<BotDTO> criarBot(@RequestBody BotDTO dto) {
        return ResponseEntity.ok(botService.criarBot(dto));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<BotDTO>> listarBots(@PathVariable Long tenantId) {
        return ResponseEntity.ok(botService.listarBotsPorTenant(tenantId));
    }

    @PostMapping("/{botId}/interagir")
    public ResponseEntity<String> interagir(@PathVariable Long botId, @RequestBody String mensagem) {
        return ResponseEntity.ok(botService.interagirComBot(botId, mensagem));
    }

 @GetMapping("/{botId}/historico")
public ResponseEntity<List<ChatMessage>> listarHistorico(@PathVariable Long botId) {
    Bot bot = botService.findBotById(botId);
    return ResponseEntity.ok(chatMessageRepository.findByBotIdAndTenantId(botId, bot.getTenant().getId()));
}
}
