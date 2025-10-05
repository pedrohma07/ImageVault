package io.pedrohma07.ImageVault.controller;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import io.pedrohma07.ImageVault.dto.auth.TwoFactorSetupDTO;
import io.pedrohma07.ImageVault.dto.auth.TwoFactorVerificationDTO;
import io.pedrohma07.ImageVault.service.TwoFactorAuthService;
import io.pedrohma07.ImageVault.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication", description = "Endpoints para gerenciamento do 2FA.")
@SecurityRequirement(name = "bearerAuth")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final static String APP_NAME = "ImageVault";
    private final UserService userService;

    @PostMapping("/setup")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Inicia a configuração do 2FA para o usuário autenticado")
    public TwoFactorSetupDTO setupTwoFactorAuth(Principal principal) {
        // 1. Pega o e-mail do usuário autenticado
        String email = principal.getName();

        // 2. Gera um novo segredo
        GoogleAuthenticatorKey secret = twoFactorAuthService.generateNewSecret();

        // 3. Gera a URL do QR Code
        String qrCodeUrl = twoFactorAuthService.getOtpAuthUrl(APP_NAME, email, secret);

        // 4. Retorna o segredo e a URL para o front-end
        return new TwoFactorSetupDTO(secret.getKey(), qrCodeUrl);
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Verifica o código TOTP e ativa o 2FA para o usuário")
    public void verifyAndEnableTwoFactorAuth(
            Principal principal,
            @Valid @RequestBody TwoFactorVerificationDTO verificationDTO
    ) {
        String email = principal.getName();

        // 1. Verifica se o código de 6 dígitos é válido para o segredo fornecido
        boolean isCodeValid = twoFactorAuthService.isTotpValid(verificationDTO.secret(), verificationDTO.code());

        if (!isCodeValid) {
            throw new BadCredentialsException("Código de verificação inválido.");
        }

        // 2. Se o código for válido, ativa permanentemente o 2FA para o usuário
        userService.enableTwoFactorAuth(email, verificationDTO.secret());
    }
}
