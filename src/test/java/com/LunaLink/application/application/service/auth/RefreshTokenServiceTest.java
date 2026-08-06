package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.RefreshTokenRepositoryPort;
import com.LunaLink.application.domain.model.auth.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("Deve criar refresh token com hash e salvar")
    void create_ShouldPersistTokenHash_AndReturnRawToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(refreshTokenRepositoryPort.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String rawToken = refreshTokenService.createRefreshToken(userId);

        // Assert
        assertNotNull(rawToken);
        assertNotEquals(rawToken, RefreshTokenService.sha256(rawToken));
        verify(refreshTokenRepositoryPort).save(argThat(entity ->
                RefreshTokenService.sha256(rawToken).equals(entity.getTokenHash())
                        && userId.equals(entity.getUserId())
                        && entity.getExpiresAt().isAfter(Instant.now())));
    }

    @Test
    @DisplayName("Deve rotacionar token, revogando o antigo")
    void rotate_ShouldRevokeOld_AndReturnNewToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String rawToken = "raw-token";
        RefreshToken current = new RefreshToken(
                RefreshTokenService.sha256(rawToken), userId, Instant.now().plus(1, ChronoUnit.DAYS));

        when(refreshTokenRepositoryPort.findByTokenHash(RefreshTokenService.sha256(rawToken)))
                .thenReturn(Optional.of(current));
        when(refreshTokenRepositoryPort.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken entity = invocation.getArgument(0);
                    if (entity.getId() == null) {
                        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
                    }
                    return entity;
                });

        // Act
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(rawToken);

        // Assert
        assertNotNull(rotated.refreshToken());
        assertEquals(userId, rotated.userId());
        assertNotNull(current.getRevokedAt());
        assertNotNull(current.getReplacedBy());
        verify(refreshTokenRepositoryPort, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve lançar exceção e revogar família quando token reutilizado")
    void rotate_ShouldThrowAndRevokeFamily_WhenTokenReused() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String rawToken = "raw-token";
        RefreshToken current = new RefreshToken(
                RefreshTokenService.sha256(rawToken), userId, Instant.now().plus(1, ChronoUnit.DAYS));
        current.revoke();

        when(refreshTokenRepositoryPort.findByTokenHash(RefreshTokenService.sha256(rawToken)))
                .thenReturn(Optional.of(current));

        // Act & Assert
        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.rotate(rawToken));
        verify(refreshTokenRepositoryPort).findActiveByUserId(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando token desconhecido")
    void rotate_ShouldThrow_WhenTokenUnknown() {
        // Arrange
        String rawToken = "unknown";
        when(refreshTokenRepositoryPort.findByTokenHash(RefreshTokenService.sha256(rawToken)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.rotate(rawToken));
    }

    @Test
    @DisplayName("Deve revogar token no logout")
    void revoke_ShouldMarkTokenRevoked() {
        // Arrange
        String rawToken = "raw-token";
        RefreshToken current = new RefreshToken(
                RefreshTokenService.sha256(rawToken), UUID.randomUUID(), Instant.now().plus(1, ChronoUnit.DAYS));

        when(refreshTokenRepositoryPort.findByTokenHash(RefreshTokenService.sha256(rawToken)))
                .thenReturn(Optional.of(current));

        // Act
        refreshTokenService.revoke(rawToken);

        // Assert
        assertNotNull(current.getRevokedAt());
        verify(refreshTokenRepositoryPort).save(current);
    }

    @Test
    @DisplayName("Deve revogar todos os tokens ativos de um usuário")
    void revokeAllForUser_ShouldRevokeActiveTokens() {
        // Arrange
        UUID userId = UUID.randomUUID();
        RefreshToken token1 = new RefreshToken("hash1", userId, Instant.now().plus(1, ChronoUnit.DAYS));
        RefreshToken token2 = new RefreshToken("hash2", userId, Instant.now().plus(1, ChronoUnit.DAYS));

        when(refreshTokenRepositoryPort.findActiveByUserId(userId)).thenReturn(java.util.List.of(token1, token2));

        // Act
        refreshTokenService.revokeAllForUser(userId);

        // Assert
        assertNotNull(token1.getRevokedAt());
        assertNotNull(token2.getRevokedAt());
        verify(refreshTokenRepositoryPort, times(2)).save(any(RefreshToken.class));
    }
}
