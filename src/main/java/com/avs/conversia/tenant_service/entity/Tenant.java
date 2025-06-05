package com.avs.conversia.tenant_service.entity;

import java.util.List;

import com.avs.conversia.bot_service.entity.Bot;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String cnpj;
    private String email;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private List<Bot> bots;

    public Tenant() {}
    public Tenant(String nome, String cnpj, String email) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Bot> getBots() { return bots; }
    public void setBots(List<Bot> bots) { this.bots = bots; }
}