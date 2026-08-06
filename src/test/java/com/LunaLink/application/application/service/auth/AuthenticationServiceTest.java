package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import com.LunaLink.application.web.dto.SecurityDTO.RefreshResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private AuthenticationService service;

    @Test
    @DisplayName("Deve autenticar e retornar par de tokens com sucesso")
    void authenticate_ShouldReturnTokenPair_WhenCredentialsValid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        Users user = new Users("User", "101", "user@email.com", "password", UserRoles.RESIDENT_ROLE);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(tokenService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user.getId())).thenReturn("refresh-token");

        // Act
        LoginResponseDTO result = service.authenticate(authDTO);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(TokenService.ACCESS_TOKEN_TTL_SECONDS, result.expiresIn());
        assertEquals("Bearer", result.tokenType());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).generateToken(user);
        verify(refreshTokenService).createRefreshToken(user.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando autenticação falhar")
    void authenticate_ShouldThrowException_WhenCredentialsInvalid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> service.authenticate(authDTO));
    }

    @Test
    @DisplayName("Deve rotacionar refresh token e retornar novo par")
    void refresh_ShouldReturnNewTokenPair() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Users user = new Users("User", "101", "user@email.com", "password", UserRoles.RESIDENT_ROLE);
        user.setId(userId);
        RefreshTokenService.RotatedRefreshToken rotated =
                new RefreshTokenService.RotatedRefreshToken(userId, "new-refresh-token");

        when(refreshTokenService.rotate("old-refresh-token")).thenReturn(rotated);
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(tokenService.generateToken(user)).thenReturn("new-access-token");

        // Act
        RefreshResponseDTO result = service.refresh("old-refresh-token");

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
    }

    @Test
    @DisplayName("Deve revogar refresh token e blacklistar access token no logout")
    void logout_ShouldRevokeRefreshAndBlacklistAccess() {
        // Act
        service.logout("refresh-token", "access-token");

        // Assert
        verify(refreshTokenService).revoke("refresh-token");
        verify(tokenRevocationService).blacklistAccessToken("access-token");
    }
}
