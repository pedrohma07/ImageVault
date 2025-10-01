package io.pedrohma07.ImageVault.exception;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String token, String message) {
        super(String.format("Falha para o token [%s]: %s", token, message));
    }
}