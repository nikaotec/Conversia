package com.avs.conversia.chat_service.dto;

import java.time.LocalDateTime;

public class ConversaDTO {
    private String id;
    private Long botId;
    private Long tenantId;
    private String mensagemUsuario;
    private String respostaBot;
    private LocalDateTime timestamp;

    public ConversaDTO() {}
    public ConversaDTO(String id, Long botId, Long tenantId, String mensagemUsuario, String respostaBot, LocalDateTime timestamp) {
        this.id = id;
        this.botId = botId;
        this.tenantId = tenantId;
        this.mensagemUsuario = mensagemUsuario;
        this.respostaBot = respostaBot;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getBotId() { return botId; }
    public void setBotId(Long botId) { this.botId = botId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getMensagemUsuario() { return mensagemUsuario; }
    public void setMensagemUsuario(String mensagemUsuario) { this.mensagemUsuario = mensagemUsuario; }
    public String getRespostaBot() { return respostaBot; }
    public void setRespostaBot(String respostaBot) { this.respostaBot = respostaBot; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}