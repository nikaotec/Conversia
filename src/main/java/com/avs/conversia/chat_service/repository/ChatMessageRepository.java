package com.avs.conversia.chat_service.repository;


  import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.avs.conversia.chat_service.entity.ChatMessage;

  public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
      List<ChatMessage> findByBotIdAndTenantId(Long botId, Long tenantId);
  }