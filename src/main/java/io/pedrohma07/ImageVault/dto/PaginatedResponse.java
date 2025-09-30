package io.pedrohma07.ImageVault.dto;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> data,
        int page,
        int limit,
        long totalElements,
        int totalPages
) {}
