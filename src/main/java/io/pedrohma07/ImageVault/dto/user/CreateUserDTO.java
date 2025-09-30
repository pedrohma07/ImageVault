package io.pedrohma07.ImageVault.dto.user;
import jakarta.validation.constraints.*;
//CREATE TABLE users (
//        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
//name VARCHAR(255) NOT NULL,
//email VARCHAR(255) NOT NULL UNIQUE,
//password VARCHAR(255) NOT NULL,
//role VARCHAR(50) NOT NULL DEFAULT 'USER',
//is_2fa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
//two_fa_secret VARCHAR(255),
//created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
//updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
//deleted_at TIMESTAMP WITH TIME ZONE
//);

public record CreateUserDTO(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "Password is required")
        // Regex to enforce a strong password policy
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long, contain at least one uppercase letter, " +
                        "one lowercase letter, one number, and one special character")
        String password
) {
}
