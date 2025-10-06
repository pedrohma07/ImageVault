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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
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
        log.info("Started setupTwoFactorAuth action");
        String email = principal.getName();

        GoogleAuthenticatorKey secret = twoFactorAuthService.generateNewSecret();

        String qrCodeUrl = twoFactorAuthService.getOtpAuthUrl(APP_NAME, email, secret);

        return new TwoFactorSetupDTO(secret.getKey(), qrCodeUrl);
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Verifica o código TOTP e ativa o 2FA para o usuário")
    public void verifyAndEnableTwoFactorAuth(
            Principal principal,
            @Valid @RequestBody TwoFactorVerificationDTO verificationDTO
    ) {
        log.info("Started verifyAndEnableTwoFactorAuth action");
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
