package com.avs.conversia.bot_service.service;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.avs.conversia.bot_service.dto.BotDTO;
import com.avs.conversia.bot_service.entity.Bot;
import com.avs.conversia.bot_service.repository.BotRepository;
import com.avs.conversia.tenant_service.entity.Tenant;
import com.avs.conversia.tenant_service.repository.TenantRepository;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;

@Service
public class BotService {
    private final BotRepository botRepository;
    private final TenantRepository tenantRepository;

    public BotService(BotRepository botRepository, TenantRepository tenantRepository) {
        this.botRepository = botRepository;
        this.tenantRepository = tenantRepository;
    }

    public BotDTO criarBot(BotDTO dto) {
        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado"));
        Bot bot = new Bot();
        bot.setNome(dto.getNome());
        bot.setModeloHuggingFace(dto.getModeloHuggingFace());
        bot.setApiKey(dto.getApiKey());
        bot.setTenant(tenant);
        bot = botRepository.save(bot);
        return new BotDTO(bot.getId(), bot.getNome(), bot.getModeloHuggingFace(), bot.getApiKey(), bot.getTenant().getId());
    }

    public List<BotDTO> listarBotsPorTenant(Long tenantId) {
        return botRepository.findByTenantId(tenantId).stream()
                .map(b -> new BotDTO(b.getId(), b.getNome(), b.getModeloHuggingFace(), b.getApiKey(), b.getTenant().getId()))
                .collect(Collectors.toList());
    }

    public String interagirComBot(Long botId, String mensagem) {
    Bot bot = botRepository.findById(botId)
            .orElseThrow(() -> new IllegalArgumentException("Bot não encontrado"));

    try {
        ChatLanguageModel chatModel =  MistralAiChatModel.builder()
            .apiKey("uaeaPH2nkwRA1VwqZCFJ48I67xylve85")
            .modelName("mistral-medium") // Melhor qualidade
            .maxTokens(1000)
            .temperature(0.7)
            .build();
        
        // HuggingFaceChatModel chatModel  = HuggingFaceChatModel.builder()
        //         .accessToken(bot.getApiKey())
        //         .modelId("zpm/Llama-3.1-PersianQA") 
        //         .waitForModel(true) // Modelo alternativo
        //         .temperature(0.7)
        //         .maxNewTokens(500)
        //         .waitForModel(true)
        //         .timeout(Duration.ofSeconds(120))  // Timeout aumentado
        //         .build();

        ConversationalChain conversationalChain = ConversationalChain.builder()
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        return conversationalChain.execute(mensagem);
    } catch (Exception e) {
        throw new RuntimeException("Erro ao interagir com o modelo: " + e.getMessage(), e);
    }
}
}