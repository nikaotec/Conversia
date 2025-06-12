package com.avs.conversia.embedding_ai_engine.repository;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.avs.conversia.embedding_ai_engine.entity.FileMetadata;

public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    List<FileMetadata> findByBotIdAndTenantId(Long botId, Long tenantId);
}
