package com.avs.conversia.bot_service.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avs.conversia.bot_service.entity.Bot;

public interface BotRepository extends JpaRepository<Bot, Long> {
    List<Bot> findByTenantId(Long tenantId);
}