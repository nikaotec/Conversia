package com.avs.conversia.embedding_ai_engine.service;


import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.springframework.web.multipart.MultipartFile;

import com.avs.conversia.embedding_ai_engine.entity.FileMetadata;

public interface EmbeddingService {
    FileMetadata uploadFile(Long botId, Long tenantId, MultipartFile file) throws IOException, TikaException;
    void indexUrl(Long botId, Long tenantId, String url) throws IOException;
    void indexDatabase(Long botId, Long tenantId);
    String findRelevantContext(Long botId, Long tenantId, String query);
}
