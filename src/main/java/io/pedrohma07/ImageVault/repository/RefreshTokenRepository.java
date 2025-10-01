package io.pedrohma07.ImageVault.repository;

import io.pedrohma07.ImageVault.model.RefreshToken;
import io.pedrohma07.ImageVault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}