CREATE TABLE dev.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id UUID NOT NULL,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY(user_id)
        REFERENCES dev.users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_token ON dev.refresh_tokens(token);