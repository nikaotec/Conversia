package com.avs.conversia.bot_service.entity;

import com.avs.conversia.tenant_service.entity.Tenant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Bot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String modeloHuggingFace;
    private String apiKey;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    public Bot() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getModeloHuggingFace() { return modeloHuggingFace; }
    public void setModeloHuggingFace(String modeloHuggingFace) { this.modeloHuggingFace = modeloHuggingFace; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
}