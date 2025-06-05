package com.avs.conversia.chat_service.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversas")
public class Conversa {
    @Id
    private String id;
    private Long botId;
    private Long tenantId;
    private String mensagemUsuario;
    private String respostaBot;
    private LocalDateTime timestamp;

    public Conversa() {}
    public Conversa(Long botId, Long tenantId, String mensagemUsuario, String respostaBot) {
        this.botId = botId;
        this.tenantId = tenantId;
        this.mensagemUsuario = mensagemUsuario;
        this.respostaBot = respostaBot;
        this.timestamp = LocalDateTime.now();
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