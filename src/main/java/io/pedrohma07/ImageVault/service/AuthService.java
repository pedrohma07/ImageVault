package io.pedrohma07.ImageVault.service;

import io.pedrohma07.ImageVault.dto.auth.AuthResponseDTO;
import io.pedrohma07.ImageVault.dto.auth.Login2faRequestDTO;
import io.pedrohma07.ImageVault.dto.auth.LoginRequestDTO;
import io.pedrohma07.ImageVault.dto.auth.RefreshTokenResponsetDTO;
import io.pedrohma07.ImageVault.exception.ResourceNotFoundException;
import io.pedrohma07.ImageVault.exception.TokenRefreshException;
import io.pedrohma07.ImageVault.model.RefreshToken;
import io.pedrohma07.ImageVault.model.User;
import io.pedrohma07.ImageVault.repository.RefreshTokenRepository;
import io.pedrohma07.ImageVault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final EncryptionService encryptionService;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.debug("Login attempt for email: {}", loginRequest.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        var user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> {
                    log.warn("Authentication failed for email: {}. User not found.", loginRequest.email());
                    return new IllegalArgumentException("Erro ao auntenticar usuário");
                });

        log.info("User authenticated successfully: {}", user.getEmail());

        //  2FA IS ACTIVE
        if (user.is2FAEnabled()) {
            log.info("2FA is enabled for user: {}. Requiring verification.", user.getEmail());
            return AuthResponseDTO.requireMfa();
        }

        var accessToken = jwtService.generateToken(user);
        var refresh = createAndSaveRefreshToken(user);

        log.info("Login successful for user: {}", user.getEmail());
        return AuthResponseDTO.withTokens(
                accessToken,
                refresh.getToken()
        );
    }

    @Transactional
    public AuthResponseDTO verify2faLogin(Login2faRequestDTO login2faRequest) {
        log.debug("Verifying 2FA login for email: {}", login2faRequest.email());
        var user = userRepository.findByEmail(login2faRequest.email())
                .orElseThrow(() -> {
                    log.warn("User not found for 2FA verification with email: {}", login2faRequest.email());
                    return new ResourceNotFoundException("Usuário não encontrado.");
                });

        String decryptedSecret = encryptionService.decrypt(user.getTwoFASecret());

        if (!twoFactorAuthService.isTotpValid(decryptedSecret, login2faRequest.code())) {
            log.warn("Invalid 2FA code for user: {}", user.getEmail());
            throw new BadCredentialsException("Código 2FA inválido.");
        }

        log.info("2FA code verified successfully for user: {}", user.getEmail());
        var accessToken = jwtService.generateToken(user);
        var refreshToken = createAndSaveRefreshToken(user);

        return AuthResponseDTO.withTokens(accessToken, refreshToken.getToken());
    }

    private RefreshToken createAndSaveRefreshToken(User user) {
        log.debug("Creating and saving new refresh token for user: {}", user.getEmail());
        refreshTokenRepository.deleteByUser(user);

        var refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(jwtService.generateRefreshToken(user))
                .expiryDate(OffsetDateTime.now().plusSeconds(604800L)) // 7 days
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenResponsetDTO refreshToken(String refreshToken) {
        log.debug("Attempting to refresh access token.");
        return refreshTokenRepository.findByToken(refreshToken)
                .map(token -> {
                    if (token.getExpiryDate().isBefore(OffsetDateTime.now())) {
                        log.warn("Refresh token has expired. Deleting token.");
                        refreshTokenRepository.delete(token);
                        throw new TokenRefreshException(token.getToken(), "Refresh token expirado. Por favor, faça login novamente.");
                    }

                    User user = token.getUser();
                    log.info("Refresh token is valid for user: {}", user.getEmail());

                    String newAccessToken = jwtService.generateToken(user);
                    log.info("New access token generated for user: {}", user.getEmail());

                    return new RefreshTokenResponsetDTO(newAccessToken);
                })
                .orElseThrow(() -> {
                    log.error("Refresh token not found.");
                    return new TokenRefreshException(refreshToken, "Refresh token não encontrado.");
                });
    }
}
