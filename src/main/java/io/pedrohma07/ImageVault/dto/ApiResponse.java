package io.pedrohma07.ImageVault.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        int statusCode,
        String message,
        T data,
        String path,
        boolean success,
        LocalDateTime timestamp
) {
}