package com.LunaLink.application.application.facades.auth;

import com.LunaLink.application.application.service.auth.AuthenticationService;
import com.LunaLink.application.infrastructure.security.LoginRateLimiter;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginFacadeTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @InjectMocks
    private LoginFacade facade;

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void login_ShouldReturnTokenPair_WhenCredentialsValid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        LoginResponseDTO responseDTO = new LoginResponseDTO("access", "refresh", 7200, "Bearer");

        when(loginRateLimiter.isLocked(authDTO.email())).thenReturn(false);
        when(loginRateLimiter.tryConsume("127.0.0.1", authDTO.email())).thenReturn(true);
        when(authenticationService.authenticate(authDTO)).thenReturn(responseDTO);

        // Act
        ResponseEntity<LoginResponseDTO> response = facade.login(authDTO, "127.0.0.1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access", response.getBody().accessToken());
        assertEquals("refresh", response.getBody().refreshToken());
    }

    @Test
    @DisplayName("Deve retornar 401 quando credenciais inválidas")
    void login_ShouldReturnUnauthorized_WhenCredentialsInvalid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "wrongPassword");
        when(loginRateLimiter.isLocked(authDTO.email())).thenReturn(false);
        when(loginRateLimiter.tryConsume("127.0.0.1", authDTO.email())).thenReturn(true);
        when(authenticationService.authenticate(authDTO)).thenThrow(new BadCredentialsException("Bad credentials"));

        // Act
        ResponseEntity<LoginResponseDTO> response = facade.login(authDTO, "127.0.0.1");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 429 quando conta bloqueada")
    void login_ShouldReturnTooManyRequests_WhenLocked() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        when(loginRateLimiter.isLocked(authDTO.email())).thenReturn(true);

        // Act
        ResponseEntity<LoginResponseDTO> response = facade.login(authDTO, "127.0.0.1");

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(authenticationService, never()).authenticate(any());
    }

    @Test
    @DisplayName("Deve retornar 429 quando rate limit excedido")
    void login_ShouldReturnTooManyRequests_WhenRateLimitExceeded() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        when(loginRateLimiter.isLocked(authDTO.email())).thenReturn(false);
        when(loginRateLimiter.tryConsume("127.0.0.1", authDTO.email())).thenReturn(false);

        // Act
        ResponseEntity<LoginResponseDTO> response = facade.login(authDTO, "127.0.0.1");

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(authenticationService, never()).authenticate(any());
    }
}
