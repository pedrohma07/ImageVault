package io.pedrohma07.ImageVault.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserDTO(
        @NotBlank(message = "O nome não pode estar vazio")
        String name
) {
}
