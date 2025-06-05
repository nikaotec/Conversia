package com.avs.conversia.bot_service.config;

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
    private String apiKey;
    @Value("${huggingface.model}")
    private String model;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return HuggingFaceChatModel.builder()
                .accessToken(apiKey)
                .modelId(model)
                .build();
    }

    @Bean
    public ConversationalChain conversationalChain(ChatLanguageModel chatLanguageModel) {
        return ConversationalChain.builder()
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}