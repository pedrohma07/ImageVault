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

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final EncryptionService encryptionService;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        var user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new IllegalArgumentException("Erro ao auntenticar usuário"));

        //  2FA ESTÁ ATIVO
        if (user.is2FAEnabled()) {

            return AuthResponseDTO.requireMfa();
        }

        var accessToken = jwtService.generateToken(user);
        var refresh = createAndSaveRefreshToken(user);

        return AuthResponseDTO.withTokens(
                accessToken,
                refresh.getToken()
        );
    }

    @Transactional
    public AuthResponseDTO verify2faLogin(Login2faRequestDTO login2faRequest) {
        var user = userRepository.findByEmail(login2faRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        String decryptedSecret = encryptionService.decrypt(user.getTwoFASecret());

        if (!twoFactorAuthService.isTotpValid(decryptedSecret, login2faRequest.code())) {
            throw new BadCredentialsException("Código 2FA inválido.");
        }

        var accessToken = jwtService.generateToken(user);
        var refreshToken = createAndSaveRefreshToken(user);

        return AuthResponseDTO.withTokens(accessToken, refreshToken.getToken());
    }

    private RefreshToken createAndSaveRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        var refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(jwtService.generateRefreshToken(user))
                .expiryDate(OffsetDateTime.now().plusSeconds(604800L)) // 7 dias
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenResponsetDTO refreshToken(String refreshToken) {

        return refreshTokenRepository.findByToken(refreshToken)
                .map(token -> {
                    if (token.getExpiryDate().isBefore(OffsetDateTime.now())) {
                        refreshTokenRepository.delete(token);
                        throw new TokenRefreshException(token.getToken(), "Refresh token expirado. Por favor, faça login novamente.");
                    }

                    User user = token.getUser();

                    String newAccessToken = jwtService.generateToken(user);

                    return new RefreshTokenResponsetDTO(newAccessToken);
                })
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token não encontrado."));
    }
}
