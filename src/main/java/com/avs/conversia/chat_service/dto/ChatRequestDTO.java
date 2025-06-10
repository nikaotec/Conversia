package com.avs.conversia.chat_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    @NotBlank(message = "Mensagem é obrigatória")
    private String message;
    private String sessionId; // Opcional, para rastrear a sessão
}
