package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenAuthenticator tokenAuthenticator;

    @InjectMocks
    private SecurityFilter securityFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar quando token válido")
    void doFilter_ShouldAuthenticate_WhenTokenValid() throws Exception {
        // Arrange
        Users user = new Users("User", "101", "user@email.com", "password", UserRoles.RESIDENT_ROLE);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(tokenAuthenticator.authenticate("valid-token")).thenReturn(user);

        // Act
        securityFilter.doFilter(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("user@email.com", authentication.getName());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    @DisplayName("Deve seguir sem autenticar quando não há token")
    void doFilter_ShouldNotAuthenticate_WhenNoToken() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // Act
        securityFilter.doFilter(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando token inválido")
    void doFilter_ShouldThrowBadCredentials_WhenTokenInvalid() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(tokenAuthenticator.authenticate("invalid-token"))
                .thenThrow(new BadCredentialsException("Token inválido ou expirado"));

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> securityFilter.doFilter(request, response, filterChain));
    }

    @Test
    @DisplayName("Deve extrair token Bearer do header")
    void recoverToken_ShouldExtractBearerToken() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc.def.ghi");

        // Act & Assert
        assertEquals("abc.def.ghi", securityFilter.recoverToken(request));
    }

    @Test
    @DisplayName("Deve retornar null quando header ausente ou sem prefixo Bearer")
    void recoverToken_ShouldReturnNull_WhenHeaderMissingOrWrongPrefix() {
        // Arrange
        MockHttpServletRequest noHeader = new MockHttpServletRequest();
        MockHttpServletRequest wrongPrefix = new MockHttpServletRequest();
        wrongPrefix.addHeader("Authorization", "Basic abc");

        // Act & Assert
        assertNull(securityFilter.recoverToken(noHeader));
        assertNull(securityFilter.recoverToken(wrongPrefix));
    }
}
