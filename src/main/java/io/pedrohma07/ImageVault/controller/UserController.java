package io.pedrohma07.ImageVault.controller;

import io.pedrohma07.ImageVault.dto.user.CreateUserDTO;
import io.pedrohma07.ImageVault.dto.PaginatedResponse;
import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.dto.user.UpdateUserDTO;
import io.pedrohma07.ImageVault.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista de todos os usuários registrados no sistema.") // 2. Descreve o endpoint
    @ApiResponses(value = { // 3. Documenta as possíveis respostas
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Token JWT inválido ou ausente."),
            @ApiResponse(responseCode = "403", description = "Proibido. O usuário não tem permissão para acessar este recurso.")
    })
    public PaginatedResponse<ResponseUserDTO> listAllUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ResponseUserDTO> users = userService.listAllUsers(pageable);

        return new PaginatedResponse<>(
                users.getContent(),
                page,
                limit,
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtém detalhes de um usuário", description = "Retorna os detalhes de um usuário específico pelo ID.") // 2. Descreve o endpoint
    @ApiResponses(value = { // 3. Documenta as possíveis respostas
            @ApiResponse(responseCode = "200", description = "Detalhes do usuário retornados com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Token JWT inválido ou ausente."),
            @ApiResponse(responseCode = "403", description = "Proibido. O usuário não tem permissão para acessar este recurso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado." )
    })
    public ResponseUserDTO getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Atualiza um usuário", description = "Atualiza os dados de um usuário específico pelo ID.") // 2. Descreve o endpoint
    @ApiResponses(value = { // 3. Documenta as possíveis respostas
            @ApiResponse(responseCode = "204", description = "Usuário atualizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Token JWT inválido ou ausente."),
            @ApiResponse(responseCode = "403", description = "Proibido. O usuário não tem permissão para acessar este recurso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado." )
    })
    public void updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        userService.updateUser(id, updateUserDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta um usuário", description = "Deleta um usuário específico pelo ID.") // 2. Descreve o endpoint
    @ApiResponses(value = { // 3. Documenta as possíveis respostas
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Token JWT inválido ou ausente."),
            @ApiResponse(responseCode = "403", description = "Proibido. O usuário não tem permissão para acessar este recurso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado." )
    })
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }
}
