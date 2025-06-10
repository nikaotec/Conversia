package com.avs.conversia.bot_service.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;

@Configuration
public class LangChainConfig {
    @Value("${huggingface.api-key}")
    private String defaultHuggingFaceApiKey;

    @Value("${huggingface.model}")
    private String defaultHuggingFaceModel;

    @Bean
    public ChatLanguageModel defaultChatLanguageModel() {
        return HuggingFaceChatModel.builder()
                .accessToken(defaultHuggingFaceApiKey)
                .modelId(defaultHuggingFaceModel)
                .temperature(0.7)
                .maxNewTokens(500)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public ConversationalChain defaultConversationalChain(ChatLanguageModel defaultChatLanguageModel) {
        return ConversationalChain.builder()
                .chatLanguageModel(defaultChatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}