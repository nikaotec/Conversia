package com.avs.conversia.embedding_ai_engine.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.avs.conversia.embedding_ai_engine.entity.UrlMetadata;

public interface UrlMetadataRepository extends MongoRepository<UrlMetadata, String> {
    List<UrlMetadata> findByBotIdAndTenantId(Long botId, Long tenantId);
}