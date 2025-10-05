package io.pedrohma07.ImageVault.handler;

import io.pedrohma07.ImageVault.dto.ApiResponse;
import io.pedrohma07.ImageVault.exception.ResourceNotFoundException;
import io.pedrohma07.ImageVault.exception.TokenRefreshException;
import io.pedrohma07.ImageVault.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return !returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();

        // Não embrulha respostas do Swagger/OpenAPI
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }

        // Evita double wrap se já for ApiResponse
        if (body instanceof ApiResponse) {
            return body;
        }

        int statusCode = ((org.springframework.http.server.ServletServerHttpResponse) response).getServletResponse().getStatus();

        return new ApiResponse<>(statusCode, "Operação realizada com sucesso.", body, path, true, LocalDateTime.now());
    }

    // Handler para "Unauthorized" (401)
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage() != null ? ex.getMessage() : "Acesso não autorizado.",
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                "E-mail ou senha inválidos. Por favor, tente novamente.",
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(TokenRefreshException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleTokenRefreshException(TokenRefreshException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    // Handler para "Forbidden"(403)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado. Você não tem permissão para executar esta ação.",
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    // Handler para "Resource Not Found" (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    // Handler para "Bad Request" - Validações de DTO com @Valid (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação.",
                errors, // O 'data' aqui contém os detalhes dos erros de validação
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    // Handler para "Conflict" - E-mail já existe (409)
    @ExceptionHandler(IllegalArgumentException.class) // Ou uma exceção customizada como EmailAlreadyExistsException
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Object> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    // Handler para "Bad Request" - Erros de cliente HTTP (400)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE) // Retorna o status 413
    public ApiResponse<Object> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Arquivo muito grande! O tamanho máximo permitido é de 10MB.",
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }

    // Handler genérico para qualquer outra exceção não tratada (500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleGenericException(Exception ex, HttpServletRequest request) {

        System.out.println(ex);

        return new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro inesperado no servidor.",
                null,
                request.getRequestURI(),
                false,
                LocalDateTime.now()
        );
    }
}