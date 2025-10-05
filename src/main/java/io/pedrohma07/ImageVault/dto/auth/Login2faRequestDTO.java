package io.pedrohma07.ImageVault.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record Login2faRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String code
) {}