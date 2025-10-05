package io.pedrohma07.ImageVault.controller;

import io.pedrohma07.ImageVault.dto.auth.*;
import io.pedrohma07.ImageVault.dto.user.CreateUserDTO;
import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.service.AuthService;
import io.pedrohma07.ImageVault.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para registro e login de usuários.")
public class AuthController {
    private final UserService userService; // Para o registro
    private final AuthService authService; // Para o login

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um novo usuário")
    public ResponseUserDTO register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        return userService.createUser(createUserDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário e retorna os tokens")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Gera um novo access token a partir de um refresh token")
    public ResponseEntity<RefreshTokenResponsetDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }

    @PostMapping("/login-2fa")
    @Operation(summary = "Verifica o código 2FA e finaliza o login")
    public ResponseEntity<AuthResponseDTO> loginWith2fa(@Valid @RequestBody Login2faRequestDTO login2faRequest) {
        return ResponseEntity.ok(authService.verify2faLogin(login2faRequest));
    }
}
