package com.avs.conversia.chat_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.avs.conversia.chat_service.entity.ChatSession;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    Optional<ChatSession> findBySessionIdAndBotId(String sessionId, Long botId);
}
