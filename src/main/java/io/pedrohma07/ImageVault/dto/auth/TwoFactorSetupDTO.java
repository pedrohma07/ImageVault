package io.pedrohma07.ImageVault.dto.auth;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public record TwoFactorSetupDTO(
        String secret,
        String qrCodeUrl
) {}