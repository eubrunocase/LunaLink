package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.auth.LoginFacade;
import com.LunaLink.application.application.service.auth.AuthenticationService;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LogoutRequestDTO;
import com.LunaLink.application.web.dto.SecurityDTO.RefreshRequestDTO;
import com.LunaLink.application.web.dto.SecurityDTO.RefreshResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private LoginFacade loginFacade;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticationController controller;

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void login_ShouldReturnTokenPair_WhenCredentialsValid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        LoginResponseDTO loginResponse = new LoginResponseDTO("access", "refresh", 7200, "Bearer");
        when(loginFacade.login(any(), any())).thenReturn(ResponseEntity.ok(loginResponse));

        // Act
        ResponseEntity<LoginResponseDTO> response = controller.login(authDTO, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access", response.getBody().accessToken());
        assertEquals("refresh", response.getBody().refreshToken());
    }

    @Test
    @DisplayName("Deve retornar erro ao falhar autenticação")
    void login_ShouldReturnUnauthorized_WhenCredentialsInvalid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "wrongPassword");
        when(loginFacade.login(any(), any())).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        // Act
        ResponseEntity<LoginResponseDTO> response = controller.login(authDTO, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve rotacionar token no refresh")
    void refresh_ShouldReturnNewTokenPair() {
        // Arrange
        RefreshRequestDTO requestDTO = new RefreshRequestDTO("old-refresh-token");
        RefreshResponseDTO responseDTO = new RefreshResponseDTO("new-access", "new-refresh", 7200, "Bearer");
        when(authenticationService.refresh("old-refresh-token")).thenReturn(responseDTO);

        // Act
        ResponseEntity<RefreshResponseDTO> response = controller.refresh(requestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-access", response.getBody().accessToken());
        assertEquals("new-refresh", response.getBody().refreshToken());
    }

    @Test
    @DisplayName("Deve revogar tokens no logout")
    void logout_ShouldRevokeRefreshAndBlacklistAccess() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO("refresh-token");

        // Act
        ResponseEntity<Void> response = controller.logout(logoutRequest, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).logout("refresh-token", "access-token");
    }
}
