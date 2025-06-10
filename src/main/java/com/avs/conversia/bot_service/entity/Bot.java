package com.avs.conversia.bot_service.entity;


import com.avs.conversia.bot_service.enums.ModelProvider;
import com.avs.conversia.tenant_service.entity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "api_key")
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private ModelProvider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}