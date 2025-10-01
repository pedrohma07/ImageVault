package io.pedrohma07.ImageVault.dto.auth;


import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank String refreshToken
) {}