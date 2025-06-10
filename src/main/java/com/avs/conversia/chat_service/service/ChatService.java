package com.avs.conversia.chat_service.service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.avs.conversia.bot_service.entity.Bot;
import com.avs.conversia.bot_service.repository.BotRepository;
import com.avs.conversia.bot_service.service.BotService;
import com.avs.conversia.chat_service.dto.ChatRequestDTO;
import com.avs.conversia.chat_service.entity.ChatMessage;
import com.avs.conversia.chat_service.entity.ChatSession;
import com.avs.conversia.chat_service.repository.ChatMessageRepository;
import com.avs.conversia.chat_service.repository.ChatSessionRepository;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final BotService botService;
    private final BotRepository botRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    public ChatService(BotService botService, BotRepository botRepository,
                       ChatMessageRepository chatMessageRepository, ChatSessionRepository chatSessionRepository) {
        this.botService = botService;
        this.botRepository = botRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionRepository = chatSessionRepository;
    }

    public String interagirComBot(Long botId, ChatRequestDTO request) {
        logger.info("Processando interação com botId: {}, sessionId: {}", botId, request.getSessionId());

        // Validar se o bot existe
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot não encontrado"));

        // Gerenciar sessão
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        ChatSession chatSession = chatSessionRepository.findBySessionIdAndBotId(sessionId, botId)
                .orElseGet(() -> ChatSession.builder()
                        .botId(botId)
                        .tenantId(bot.getTenant().getId())
                        .sessionId(sessionId)
                        .messageIds(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .lastUpdatedAt(LocalDateTime.now())
                        .build());

        // Chamar o BotService para processar a mensagem
        String resposta = botService.interagirComBot(botId, request.getMessage());

        // Atualizar a sessão
        ChatMessage chatMessage = chatMessageRepository.findByBotIdAndTenantId(botId, bot.getTenant().getId())
                .stream()
                .filter(msg -> msg.getUserMessage().equals(request.getMessage()) && msg.getBotResponse().equals(resposta))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mensagem não encontrada após interação"));
        chatSession.getMessageIds().add(chatMessage.getId());
        chatSession.setLastUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(chatSession);

        logger.info("Resposta gerada para botId: {}, sessionId: {}", botId, sessionId);
        return resposta;
    }
}