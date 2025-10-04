package io.pedrohma07.ImageVault.dto.image;

import io.pedrohma07.ImageVault.model.enums.ImageVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateImageMetadataDTO(
        @NotNull(message = "A visibilidade não pode ser nula.")
        ImageVisibility visibility,
        @NotBlank(message = "O nome do arquivo não pode estar em branco.")
        String fileName
) {}