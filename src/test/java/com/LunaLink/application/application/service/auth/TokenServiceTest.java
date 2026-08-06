package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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
        Users user = new Users("User", "101", "testUser@email.com", "password", UserRoles.RESIDENT_ROLE);

        // Act
        String token = tokenService.generateToken(user);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve validar token com claims completos")
    void validateToken_ShouldReturnDecodedToken_WhenTokenValid() {
        // Arrange
        Users user = new Users("User", "101", "testUser@email.com", "password", UserRoles.RESIDENT_ROLE);
        user.setTokenVersion(2);
        String token = tokenService.generateToken(user);

        // Act
        DecodedJWT decoded = tokenService.validateToken(token);

        // Assert
        assertEquals("testUser@email.com", decoded.getSubject());
        assertEquals("lunalink-api", decoded.getAudience().get(0));
        assertNotNull(decoded.getIssuedAt());
        assertNotNull(decoded.getId());
        assertEquals(2, decoded.getClaim("token_version").asInt());
    }

    @Test
    @DisplayName("Deve lançar exceção para token inválido")
    void validateToken_ShouldThrow_WhenTokenInvalid() {
        // Act & Assert
        assertThrows(JWTVerificationException.class, () -> tokenService.validateToken("invalidToken"));
    }

    @Test
    @DisplayName("Deve lançar exceção para token com audience incorreta")
    void validateToken_ShouldThrow_WhenAudienceIncorrect() throws Exception {
        // Arrange
        Users user = new Users("User", "101", "testUser@email.com", "password", UserRoles.RESIDENT_ROLE);
        String secret = "testSecret";
        com.auth0.jwt.algorithms.Algorithm algorithm = com.auth0.jwt.algorithms.Algorithm.HMAC256(secret);
        String token = com.auth0.jwt.JWT.create()
                .withIssuer("User")
                .withSubject(user.getEmail())
                .withAudience("outra-api")
                .sign(algorithm);

        // Act & Assert
        assertThrows(JWTVerificationException.class, () -> tokenService.validateToken(token));
    }
}
