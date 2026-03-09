package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private AuthorizationUserService service;

    @Test
    @DisplayName("Deve carregar usuário por username com sucesso")
    void loadUserByUsername_ShouldReturnUserDetails_WhenFound() {
        // Arrange
        String username = "testUser@email.com";
        Users user = new Users("User", "101", username, "pass", UserRoles.RESIDENT_ROLE);
        when(userRepositoryPort.findByEmail(username)).thenReturn(user);

        // Act
        UserDetails result = service.loadUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void loadUserByUsername_ShouldThrowException_WhenNotFound() {
        // Arrange
        String username = "unknownUser@email.com";
        when(userRepositoryPort.findByEmail(username)).thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(username));
    }
}
