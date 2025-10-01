package io.pedrohma07.ImageVault.dto.auth;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken) {
}
