package com.LunaLink.application.infrastructure.repository.auth;

import com.LunaLink.application.application.ports.output.RefreshTokenRepositoryPort;
import com.LunaLink.application.domain.model.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID>, RefreshTokenRepositoryPort {

    @Override
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Override
    default List<RefreshToken> findActiveByUserId(UUID userId) {
        return findByUserIdAndRevokedAtIsNull(userId);
    }

    List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId);
}
