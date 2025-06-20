package com.avs.conversia.bot_service.config;

import com.avs.conversia.bot_service.service.BotService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class KnowledgeBaseLoader {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseLoader.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Autowired
    public KnowledgeBaseLoader(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    @PostConstruct
    public void loadData() {
        logger.info("Starting to load knowledge base data...");

        List<Document> documents = Arrays.asList(
                Document.from("Java is a versatile programming language used for enterprise applications.", Metadata.from("source", "manual-input-1")),
                Document.from("Spring Boot is a popular framework for building Java microservices.", Metadata.from("source", "manual-input-2")),
                Document.from("Langchain4j helps create LLM-powered applications in Java.", Metadata.from("source", "manual-input-3")),
                Document.from("To check similarity, the bot needs to compare user questions with known information.", Metadata.from("source", "manual-input-4"))
        );

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();

        ingestor.ingest(documents);

        logger.info("{} documents were ingested into the embedding store.", documents.size());
    }
}
