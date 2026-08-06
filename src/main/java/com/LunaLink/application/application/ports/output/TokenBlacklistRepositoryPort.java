package com.LunaLink.application.application.ports.output;

import java.time.Instant;

public interface TokenBlacklistRepositoryPort {
    boolean isBlacklisted(String jti);
    void blacklist(String jti, Instant expiresAt);
}
