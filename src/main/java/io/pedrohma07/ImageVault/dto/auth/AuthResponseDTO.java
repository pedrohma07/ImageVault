package io.pedrohma07.ImageVault.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        Boolean mfaRequired
) {
    public static AuthResponseDTO withTokens(String accessToken, String refreshToken) {
        return new AuthResponseDTO(accessToken, refreshToken, null);
    }

    public static AuthResponseDTO requireMfa() {
        return new AuthResponseDTO(null, null, true);
    }
}
