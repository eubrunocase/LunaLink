package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.auth.LoginFacade;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private LoginFacade loginFacade;

    @InjectMocks
    private AuthenticationController controller;

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void login_ShouldReturnToken_WhenCredentialsValid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        LoginResponseDTO loginResponse = new LoginResponseDTO("token");
        when(loginFacade.login(authDTO)).thenReturn(ResponseEntity.ok(loginResponse));

        // Act
        ResponseEntity<String> response = controller.login(authDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token", response.getBody());
    }

    @Test
    @DisplayName("Deve retornar erro ao falhar autenticação")
    void login_ShouldReturnBadRequest_WhenCredentialsInvalid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "wrongPassword");
        when(loginFacade.login(authDTO)).thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<String> response = controller.login(authDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
