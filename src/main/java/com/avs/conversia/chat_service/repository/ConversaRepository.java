package com.avs.conversia.chat_service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.avs.conversia.chat_service.entity.Conversa;

public interface ConversaRepository extends MongoRepository<Conversa, String> {
    List<Conversa> findByBotIdAndTenantId(Long botId, Long tenantId);
}