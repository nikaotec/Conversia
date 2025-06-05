package com.avs.conversia.tenant_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avs.conversia.tenant_service.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}