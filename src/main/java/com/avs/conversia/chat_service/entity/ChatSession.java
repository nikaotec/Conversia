package com.avs.conversia.chat_service.entity;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
    @Id
    private String id;
    private Long botId;
    private Long tenantId;
    private String sessionId; // Identificador único da sessão do usuário final
    private List<String> messageIds; // Referências aos IDs das mensagens (ChatMessage)
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
