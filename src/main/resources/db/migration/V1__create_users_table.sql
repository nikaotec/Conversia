-- V1__create_users_table.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    tenant_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);