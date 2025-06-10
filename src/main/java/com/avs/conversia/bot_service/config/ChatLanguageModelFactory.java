package com.avs.conversia.bot_service.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.avs.conversia.bot_service.enums.ModelProvider;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;

@Component
public class ChatLanguageModelFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChatLanguageModelFactory.class);

    @Value("${huggingface.api-key:}")
    private String defaultHuggingFaceApiKey;

    @Value("${mistral.api-key:}")
    private String defaultMistralApiKey;

    public ChatLanguageModel createChatLanguageModel(ModelProvider provider, String apiKey, String model) {
        logger.debug("Criando ChatLanguageModel para provedor: {}, modelo: {}", provider, model);

        // Usar a apiKey do Bot, ou fallback para a chave padrão
        String effectiveApiKey = apiKey != null && !apiKey.trim().isEmpty() 
            ? apiKey 
            : (provider == ModelProvider.HUGGING_FACE ? defaultHuggingFaceApiKey : defaultMistralApiKey);

        if (effectiveApiKey == null || effectiveApiKey.trim().isEmpty()) {
            logger.error("Chave de API não fornecida para o provedor: {}", provider);
            throw new IllegalArgumentException("Chave de API não fornecida para o provedor: " + provider);
        }

        switch (provider) {
            case HUGGING_FACE:
                logger.info("Criando HuggingFaceChatModel com modelo: {}", model);
                return HuggingFaceChatModel.builder()
                        .accessToken(effectiveApiKey)
                        .modelId(model)
                        .temperature(0.1)
                        .maxNewTokens(500)
                        .timeout(Duration.ofSeconds(120))
                        .build();
            case MISTRAL_AI:
                logger.info("Criando MistralAiChatModel com modelo: {}", model);
                return MistralAiChatModel.builder()
                        .apiKey(effectiveApiKey)
                        .modelName(model)
                        .maxTokens(1000)
                        .temperature(0.7)
                        .build();
            default:
                logger.error("Provedor não suportado: {}", provider);
                throw new IllegalArgumentException("Provedor não suportado: " + provider);
        }
    }
}