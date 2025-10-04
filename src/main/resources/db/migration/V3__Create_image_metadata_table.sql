CREATE TYPE image_visibility AS ENUM ('PRIVATE', 'PUBLIC');

CREATE TABLE dev.image_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    storage_key VARCHAR(512) NOT NULL UNIQUE,
    visibility image_visibility NOT NULL DEFAULT 'PRIVATE',
    owner_id UUID NOT NULL,
    upload_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_metadata_owner
        FOREIGN KEY(owner_id)
        REFERENCES dev.users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_image_metadata_storage_key ON dev.image_metadata(storage_key);

CREATE INDEX idx_image_metadata_owner_id ON dev.image_metadata(owner_id);