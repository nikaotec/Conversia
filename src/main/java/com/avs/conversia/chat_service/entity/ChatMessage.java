package com.avs.conversia.chat_service.entity;

  import java.time.LocalDateTime;

  import org.springframework.data.annotation.Id;
  import org.springframework.data.mongodb.core.mapping.Document;

  import lombok.AllArgsConstructor;
  import lombok.Builder;
  import lombok.Getter;
  import lombok.NoArgsConstructor;
  import lombok.Setter;

  @Document(collection = "chat_messages")
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public class ChatMessage {
      @Id
      private String id;
      private Long botId;
      private Long tenantId;
      private String userMessage;
      private String botResponse;
      private LocalDateTime timestamp;
  }
  