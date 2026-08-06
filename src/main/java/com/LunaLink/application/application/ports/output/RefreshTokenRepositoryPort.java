package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.auth.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    RefreshToken save(RefreshToken refreshToken);
    List<RefreshToken> findActiveByUserId(UUID userId);
}
