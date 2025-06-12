package com.avs.conversia.embedding_ai_engine.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.avs.conversia.embedding_ai_engine.entity.Embedding;

public interface EmbeddingRepository extends MongoRepository<Embedding, String> {
    List<Embedding> findByBotIdAndTenantId(Long botId, Long tenantId);
}