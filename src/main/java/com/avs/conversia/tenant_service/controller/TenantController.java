package com.avs.conversia.tenant_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avs.conversia.tenant_service.dto.TenantDTO;
import com.avs.conversia.tenant_service.service.TenantService;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TenantDTO> salvar(@RequestBody TenantDTO dto) {
        return ResponseEntity.ok(service.salvar(dto));
    }

    @GetMapping
    public ResponseEntity<List<TenantDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }
}