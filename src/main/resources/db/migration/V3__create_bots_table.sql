-- Criar tabela bots
CREATE TABLE bots (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    modelo VARCHAR(255),
    api_key VARCHAR(255),
    provider VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL,
    CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT check_provider CHECK (provider IN ('HUGGING_FACE', 'MISTRAL_AI'))
);

-- Adicionar índices para melhorar performance
CREATE INDEX idx_bots_tenant_id ON bots(tenant_id);