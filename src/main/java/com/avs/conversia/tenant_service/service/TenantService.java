package com.avs.conversia.tenant_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avs.conversia.auth_service.entity.User;
import com.avs.conversia.auth_service.repository.UserRepository;
import com.avs.conversia.tenant_service.entity.Tenant;
import com.avs.conversia.tenant_service.repository.TenantRepository;

@Service
public class TenantService {
    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public TenantService(TenantRepository tenantRepository, UserRepository userRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"))
            .getId();
    }

    @Transactional
    public Tenant createTenant(Long userId, String name, String cnpj, String email) {
        logger.info("Creating tenant {} for user {}", name, userId);
        if (tenantRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Tenant name already exists");
        }
        if (tenantRepository.findByCnpj(cnpj).isPresent()) {
            throw new IllegalArgumentException("CNPJ already exists");
        }
        

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setCnpj(cnpj);
        tenant.setEmail(email);
        tenant.setUser(user);
        return tenantRepository.save(tenant);
    }
}