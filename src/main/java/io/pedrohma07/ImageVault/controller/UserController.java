package io.pedrohma07.ImageVault.controller;

import io.pedrohma07.ImageVault.dto.user.CreateUserDTO;
import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "user", description = "Endpoints para registro e listagem de usuários.")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um novo usuário", description = "Cria uma nova conta de usuário no sistema.") // 2. Descreve o endpoint
    @ApiResponses(value = { // 3. Documenta as possíveis respostas
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: e-mail ou senha fora do padrão)."),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado no sistema.") // 'Conflict' é o status para email já existente
    })
    public ResponseUserDTO createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        return userService.createUser(createUserDTO);
    }
}
