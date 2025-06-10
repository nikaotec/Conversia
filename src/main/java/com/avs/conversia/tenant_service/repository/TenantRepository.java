package com.avs.conversia.tenant_service.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avs.conversia.tenant_service.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    Optional<Tenant> findByCnpj(String cnpj);
    Optional<Tenant> findByEmail(String email);
}