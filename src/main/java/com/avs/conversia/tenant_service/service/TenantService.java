package com.avs.conversia.tenant_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.avs.conversia.tenant_service.dto.TenantDTO;
import com.avs.conversia.tenant_service.entity.Tenant;
import com.avs.conversia.tenant_service.repository.TenantRepository;

@Service
public class TenantService {
    private final TenantRepository repository;

    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    public TenantDTO salvar(TenantDTO dto) {
        Tenant tenant = new Tenant(dto.getNome(), dto.getCnpj(), dto.getEmail());
        tenant = repository.save(tenant);
        return new TenantDTO(tenant.getId(), tenant.getNome(), tenant.getCnpj(), tenant.getEmail());
    }

    public List<TenantDTO> listarTodos() {
        return repository.findAll().stream()
                .map(t -> new TenantDTO(t.getId(), t.getNome(), t.getCnpj(), t.getEmail()))
                .collect(Collectors.toList());
    }
}