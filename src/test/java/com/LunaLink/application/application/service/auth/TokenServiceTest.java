package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", "testSecret");
    }

    @Test
    @DisplayName("Deve gerar token válido para usuário")
    void generateToken_ShouldReturnToken_WhenUserValid() {
        // Arrange
        Users user = new Users("testUser", "password", UserRoles.RESIDENT_ROLE);

        // Act
        String token = tokenService.generateToken(user);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve validar token corretamente")
    void validateToken_ShouldReturnSubject_WhenTokenValid() {
        // Arrange
        Users user = new Users("testUser", "password", UserRoles.RESIDENT_ROLE);
        String token = tokenService.generateToken(user);

        // Act
        String subject = tokenService.validateToken(token);

        // Assert
        assertEquals("testUser", subject);
    }

    @Test
    @DisplayName("Deve retornar erro para token inválido")
    void validateToken_ShouldReturnError_WhenTokenInvalid() {
        // Act
        String result = tokenService.validateToken("invalidToken");

        // Assert
        assertEquals("Invalid token", result);
    }
}
