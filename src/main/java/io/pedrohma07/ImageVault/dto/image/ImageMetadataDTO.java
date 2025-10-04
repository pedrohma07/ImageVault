package io.pedrohma07.ImageVault.dto.image;

import io.pedrohma07.ImageVault.model.enums.ImageVisibility;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ImageMetadataDTO(
        UUID id,
        String fileName,
        String contentType,
        long size,
        ImageVisibility visibility,
        UUID ownerId,
        OffsetDateTime uploadTimestamp
) {}