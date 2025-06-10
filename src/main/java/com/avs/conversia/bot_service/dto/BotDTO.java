package com.avs.conversia.bot_service.dto;



import com.avs.conversia.bot_service.enums.ModelProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotDTO {
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    private String modelo;

    private String apiKey;

    @NotNull(message = "Provedor é obrigatório")
    private ModelProvider provider;

    @NotNull(message = "Tenant ID é obrigatório")
    private Long tenantId;
}