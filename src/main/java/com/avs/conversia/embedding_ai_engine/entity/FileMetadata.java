package com.avs.conversia.embedding_ai_engine.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    @Id
    private String id;
    private Long botId;
    private Long tenantId;
    private String fileName;
    private String fileType;
    private String gridFsId;
    private LocalDateTime uploadedAt;
}