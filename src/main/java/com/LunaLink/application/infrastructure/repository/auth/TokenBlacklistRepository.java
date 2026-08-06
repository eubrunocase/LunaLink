package com.LunaLink.application.infrastructure.repository.auth;

import com.LunaLink.application.application.ports.output.TokenBlacklistRepositoryPort;
import com.LunaLink.application.domain.model.auth.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID>, TokenBlacklistRepositoryPort {

    boolean existsByJti(String jti);

    @Override
    default boolean isBlacklisted(String jti) {
        return existsByJti(jti);
    }

    @Override
    default void blacklist(String jti, Instant expiresAt) {
        if (!existsByJti(jti)) {
            save(new TokenBlacklist(jti, expiresAt));
        }
    }
}
