package com.avs.conversia.tenant_service.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avs.conversia.tenant_service.entity.Tenant;
import com.avs.conversia.tenant_service.service.TenantService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody CreateTenantRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); 
         // Busca o userId com base no e-mail
        Long userId = tenantService.getUserIdByEmail(email);
        Tenant tenant = tenantService.createTenant(userId, request.getName(), request.getCnpj(), email);
        return ResponseEntity.ok(tenant);
    }
}

class CreateTenantRequest {
    private Long userId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "CNPJ is required")
    @Pattern(regexp = "\\d{14}", message = "CNPJ must be 14 digits")
    private String cnpj;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

}