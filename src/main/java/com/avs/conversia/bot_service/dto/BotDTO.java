package com.avs.conversia.bot_service.dto;


public class BotDTO {
    private Long id;
    private String nome;
    private String modeloHuggingFace;
    private String apiKey;
    private Long tenantId;

    public BotDTO() {}
    public BotDTO(Long id, String nome, String modeloHuggingFace, String apiKey, Long tenantId) {
        this.id = id;
        this.nome = nome;
        this.modeloHuggingFace = modeloHuggingFace;
        this.apiKey = apiKey;
        this.tenantId = tenantId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getModeloHuggingFace() { return modeloHuggingFace; }
    public void setModeloHuggingFace(String modeloHuggingFace) { this.modeloHuggingFace = modeloHuggingFace; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}