package com.avs.conversia.bot_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.avs.conversia.bot_service.config.ChatLanguageModelFactory;
import com.avs.conversia.bot_service.dto.BotDTO;
import com.avs.conversia.bot_service.entity.Bot;
import com.avs.conversia.bot_service.repository.BotRepository;
import com.avs.conversia.chat_service.entity.ChatMessage;
import com.avs.conversia.chat_service.repository.ChatMessageRepository;
import com.avs.conversia.tenant_service.entity.Tenant;
import com.avs.conversia.tenant_service.repository.TenantRepository;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;

@Service
public class BotService {
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final BotRepository botRepository;
    private final TenantRepository tenantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatLanguageModelFactory chatLanguageModelFactory;
    private final ConversationalChain defaultConversationalChain;
    private final Map<Long, ChatLanguageModel> chatModelCache = new ConcurrentHashMap<>();

    @Autowired
    public BotService(BotRepository botRepository, TenantRepository tenantRepository,
                     ChatMessageRepository chatMessageRepository,
                     ChatLanguageModelFactory chatLanguageModelFactory,
                     ConversationalChain defaultConversationalChain) {
        this.botRepository = botRepository;
        this.tenantRepository = tenantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatLanguageModelFactory = chatLanguageModelFactory;
        this.defaultConversationalChain = defaultConversationalChain;
    }

    public BotDTO criarBot(BotDTO dto) {
        String userEmail = getCurrentUserEmail();
        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado"));
        if (!tenant.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Usuário não autorizado para este tenant");
        }
        Bot bot = Bot.builder()
                .name(dto.getName())
                .modelo(dto.getModelo())
                .apiKey(dto.getApiKey())
                .provider(dto.getProvider())
                .tenant(tenant)
                .build();
        bot = botRepository.save(bot);
        return new BotDTO(bot.getId(), bot.getName(), bot.getModelo(), bot.getApiKey(), bot.getProvider(), bot.getTenant().getId());
    }

    public List<BotDTO> listarBotsPorTenant(Long tenantId) {
        String userEmail = getCurrentUserEmail();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado"));
        if (!tenant.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Usuário não autorizado para este tenant");
        }
        return botRepository.findByTenantId(tenantId).stream()
                .map(b -> new BotDTO(b.getId(), b.getName(), b.getModelo(), b.getApiKey(), b.getProvider(), b.getTenant().getId()))
                .collect(Collectors.toList());
    }

    public String interagirComBot(Long botId, String mensagem) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot não encontrado"));

        try {
            logger.info("Interagindo com o bot {} usando o provedor {} e modelo {}", botId, bot.getProvider(), bot.getModelo());

            // Validar modelo
            if (bot.getModelo() == null || bot.getModelo().trim().isEmpty()) {
                throw new IllegalArgumentException("Modelo não configurado para o bot: " + botId);
            }

            // Obter ou criar ChatLanguageModel do cache
            ChatLanguageModel chatModel = chatModelCache.computeIfAbsent(botId, id ->
                    chatLanguageModelFactory.createChatLanguageModel(
                            bot.getProvider(),
                            bot.getApiKey(),
                            bot.getModelo()
                    )
            );

            // Carregar histórico de mensagens do MongoDB
            List<ChatMessage> historico = chatMessageRepository.findByBotIdAndTenantId(botId, bot.getTenant().getId());
            MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
            historico.forEach(msg -> {
                chatMemory.add(new UserMessage(msg.getUserMessage()));
                chatMemory.add(new AiMessage(msg.getBotResponse()));
            });

            // Criar ConversationalChain para a interação
            ConversationalChain conversationalChain = ConversationalChain.builder()
                    .chatLanguageModel(chatModel)
                    .chatMemory(chatMemory)
                    .build();

            // Executar a interação
            String resposta = conversationalChain.execute(mensagem);
            logger.info("Resposta do bot {}: {}", botId, resposta);

            // Salvar a mensagem no MongoDB
            ChatMessage chatMessage = ChatMessage.builder()
                    .botId(botId)
                    .tenantId(bot.getTenant().getId())
                    .userMessage(mensagem)
                    .botResponse(resposta)
                    .timestamp(LocalDateTime.now())
                    .build();
            chatMessageRepository.save(chatMessage);

            return resposta;
        } catch (Exception e) {
            logger.error("Erro ao interagir com o bot {}: {}", botId, e.getMessage(), e);
            if (e.getMessage().contains("401")) {
                chatModelCache.remove(botId);
                throw new RuntimeException("Chave de API inválida para o provedor " + bot.getProvider() + ". Verifique a configuração do bot.", e);
            }
            throw new RuntimeException("Erro ao interagir com o modelo: " + e.getMessage(), e);
        }
    }

    public Bot findBotById(Long botId) {
        String userEmail = getCurrentUserEmail();
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot não encontrado"));
        if (!bot.getTenant().getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Usuário não autorizado para este bot");
        }
        return bot;
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
}
