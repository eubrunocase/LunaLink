package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.RefreshTokenRepositoryPort;
import com.LunaLink.application.domain.model.auth.RefreshToken;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    public static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
    }

    public String createRefreshToken(UUID userId) {
        String rawToken = generateSecureToken();
        RefreshToken entity = new RefreshToken(
                sha256(rawToken),
                userId,
                Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS));
        refreshTokenRepositoryPort.save(entity);
        return rawToken;
    }

    public RotatedRefreshToken rotate(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken current = refreshTokenRepositoryPort.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido"));

        if (current.getRevokedAt() != null) {
            revokeAllForUser(current.getUserId());
            throw new InvalidRefreshTokenException("Refresh token reutilizado");
        }

        if (current.isExpired()) {
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }

        String newRawToken = generateSecureToken();
        RefreshToken replacement = new RefreshToken(
                sha256(newRawToken),
                current.getUserId(),
                Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS));
        replacement = refreshTokenRepositoryPort.save(replacement);

        current.revokeWithReplacement(replacement.getId());
        refreshTokenRepositoryPort.save(current);

        return new RotatedRefreshToken(current.getUserId(), newRawToken);
    }

    public void revoke(String rawToken) {
        if (rawToken == null) {
            return;
        }
        refreshTokenRepositoryPort.findByTokenHash(sha256(rawToken)).ifPresent(entity -> {
            entity.revoke();
            refreshTokenRepositoryPort.save(entity);
        });
    }

    public void revokeAllForUser(UUID userId) {
        List<RefreshToken> active = refreshTokenRepositoryPort.findActiveByUserId(userId);
        active.forEach(entity -> {
            entity.revoke();
            refreshTokenRepositoryPort.save(entity);
        });
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }

    public record RotatedRefreshToken(UUID userId, String refreshToken) {
    }
}
