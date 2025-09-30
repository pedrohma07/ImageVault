package io.pedrohma07.ImageVault.dto.user;

public record ResponseUserDTO (
        String id,
        String name,
        String email,
        String role,
        boolean is2FAEnabled,
        String createdAt,
        String updatedAt
) {

}