-- Habilita a extensão pgcrypto se ainda não estiver habilitada,
-- para podermos usar gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Cria a tabela de usuários
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_2fa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_fa_secret VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Adiciona um índice na coluna de email para otimizar buscas
CREATE INDEX idx_users_email ON users(email);

-- Opcional: Trigger para atualizar 'updated_at' automaticamente em cada update.
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();