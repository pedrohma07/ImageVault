package io.pedrohma07.ImageVault.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorVerificationDTO(
        @NotBlank String secret,
        @NotBlank String code
) {}