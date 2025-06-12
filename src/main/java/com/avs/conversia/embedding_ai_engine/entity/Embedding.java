package com.avs.conversia.embedding_ai_engine.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "embeddings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Embedding {
    @Id
    private String id;
    private Long botId;
    private Long tenantId;
    private String fileMetadataId;
    private String urlMetadataId;
    private String dbRecordId;
    private String textChunk;
    private float[] vector;
}