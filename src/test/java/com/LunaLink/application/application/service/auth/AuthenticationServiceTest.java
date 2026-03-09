package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthenticationService service;

    @Test
    @DisplayName("Deve autenticar e retornar token com sucesso")
    void authenticate_ShouldReturnToken_WhenCredentialsValid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "password");
        Users user = new Users("User", "101", "user@email.com", "password", UserRoles.RESIDENT_ROLE);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(tokenService.generateToken(user)).thenReturn("token");

        // Act
        String token = service.authenticate(authDTO);

        // Assert
        assertNotNull(token);
        assertEquals("token", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).generateToken(user);
    }

    @Test
    @DisplayName("Deve lançar exceção quando autenticação falhar")
    void authenticate_ShouldThrowException_WhenCredentialsInvalid() {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.authenticate(authDTO));
    }
}
