package io.pedrohma07.ImageVault.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwoFactorAuthService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();


    //  (Base32).
    public GoogleAuthenticatorKey generateNewSecret() {
         return gAuth.createCredentials();
    }


     // Gera uma URI 'otpauth://' que pode ser usada para criar um QR Code.
    public String getOtpAuthUrl(String issuer, String email, GoogleAuthenticatorKey secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, email, secret);
    }


    // Valida um código TOTP fornecido pelo usuário contra o segredo armazenado.
    public boolean isTotpValid(String secret, String code) {
        try {
            // A biblioteca espera um inteiro, então convertemos a string
            int numericCode = Integer.parseInt(code);
            return gAuth.authorize(secret, numericCode);
        } catch (NumberFormatException e) {
            // Se o código não for um número válido, a validação falha.
            log.warn("Código TOTP inválido recebido: {}", code);
            return false;
        }
    }
}