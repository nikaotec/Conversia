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

import dev.langchain4j.chain.ConversationalChain; // Will be removed effectively
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage; // Will be used for chat history saving
import dev.langchain4j.data.message.ChatMessage; // Langchain's ChatMessage
import dev.langchain4j.data.message.SystemMessage; // For potential system instructions
import dev.langchain4j.data.message.UserMessage; // Will be used for chat history saving
import dev.langchain4j.data.segment.TextSegment;
// import dev.langchain4j.memory.chat.MessageWindowChatMemory; // Will be removed effectively
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.ArrayList; // For constructing message list if needed

@Service
public class BotService {
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final BotRepository botRepository;
    private final TenantRepository tenantRepository;
    private final ChatMessageRepository chatMessageRepository; // Application's ChatMessage
    private final ChatLanguageModelFactory chatLanguageModelFactory;
    // private final ConversationalChain defaultConversationalChain; // Removed
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore; // Specified TextSegment
    private final Map<Long, ChatLanguageModel> chatModelCache = new ConcurrentHashMap<>();

    @Autowired
    public BotService(BotRepository botRepository, TenantRepository tenantRepository,
                     com.avs.conversia.chat_service.repository.ChatMessageRepository chatMessageRepository, // Fully qualified
                     ChatLanguageModelFactory chatLanguageModelFactory,
                     // ConversationalChain defaultConversationalChain, // Removed
                     EmbeddingModel embeddingModel,
                     EmbeddingStore<TextSegment> embeddingStore) { // Specified TextSegment
        this.botRepository = botRepository;
        this.tenantRepository = tenantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatLanguageModelFactory = chatLanguageModelFactory;
        // this.defaultConversationalChain = defaultConversationalChain; // Removed
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
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

            // Generate embedding for the user's message
            Embedding embedding = embeddingModel.embed(mensagem).content();

            // Find relevant text segments - increased count and added minScore
            List<EmbeddingMatch<TextSegment>> relevantSegments = embeddingStore.findRelevant(embedding, 1, 0.7);

            String contextForPrompt = "";
            if (relevantSegments != null && !relevantSegments.isEmpty() && relevantSegments.get(0).score() >= 0.7) {
                TextSegment firstMatch = relevantSegments.get(0).embedded();
                contextForPrompt = "Based on the available information: \n\"" + firstMatch.text() + "\"\n\n";
                logger.info("Retrieved relevant segment: score={}, text='{}'", relevantSegments.get(0).score(), firstMatch.text());
            } else {
                logger.info("No relevant segments found (or score below threshold) for message: '{}'", mensagem);
            }

            String userQueryWithContext = contextForPrompt + "User question: " + mensagem;
            logger.info("Prompt for LLM: {}", userQueryWithContext);

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

            // Carregar histórico de mensagens do MongoDB - This part is temporarily bypassed for RAG focus
            // List<com.avs.conversia.chat_service.entity.ChatMessage> historico = chatMessageRepository.findByBotIdAndTenantId(botId, bot.getTenant().getId());
            // MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
            // historico.forEach(msg -> {
            // chatMemory.add(new UserMessage(msg.getUserMessage()));
            // chatMemory.add(new AiMessage(msg.getBotResponse()));
            // });

            // Criar ConversationalChain para a interação - Bypassed
            // ConversationalChain conversationalChain = ConversationalChain.builder()
            // .chatLanguageModel(chatModel)
            // .chatMemory(chatMemory)
            // .build();

            // Executar a interação
            // String resposta = conversationalChain.execute(mensagem); // Bypassed

            // Directly use chatModel to generate response with context
            AiMessage aiResponse = chatModel.generate(userQueryWithContext).content();
            String resposta = aiResponse.text();
            logger.info("Resposta do bot {}: {}", botId, resposta);

            // Salvar a mensagem no MongoDB
            com.avs.conversia.chat_service.entity.ChatMessage chatMessage = com.avs.conversia.chat_service.entity.ChatMessage.builder()
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
