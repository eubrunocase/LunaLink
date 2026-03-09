package com.LunaLink.application.application.facades.auth;

import com.LunaLink.application.application.service.auth.AuthenticationService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginFacadeTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private LoginFacade facade;

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void login_ShouldReturnToken_WhenCredentialsValid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        when(authenticationService.authenticate(authDTO)).thenReturn("token");

        // Act
        ResponseEntity<LoginResponseDTO> response = facade.login(authDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token", response.getBody().token());
    }

    @Test
    @DisplayName("Deve retornar erro ao falhar autenticação")
    void login_ShouldReturnBadRequest_WhenCredentialsInvalid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "wrongPassword");
        when(authenticationService.authenticate(authDTO)).thenThrow(new RuntimeException("Bad credentials"));

        // Act
        ResponseEntity<LoginResponseDTO> response = facade.login(authDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
