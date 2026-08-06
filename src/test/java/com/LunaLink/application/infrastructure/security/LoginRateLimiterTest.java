package com.LunaLink.application.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter loginRateLimiter;

    @BeforeEach
    void setUp() {
        loginRateLimiter = new LoginRateLimiter();
    }

    @Test
    @DisplayName("Não deve estar bloqueada inicialmente")
    void isLocked_ShouldReturnFalse_Initially() {
        assertFalse(loginRateLimiter.isLocked("user@email.com"));
    }

    @Test
    @DisplayName("Deve bloquear após 5 falhas")
    void shouldLock_AfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            loginRateLimiter.onAuthenticationFailure("user@email.com");
        }

        assertTrue(loginRateLimiter.isLocked("user@email.com"));
        assertFalse(loginRateLimiter.tryConsume("127.0.0.1", "user@email.com"));
    }

    @Test
    @DisplayName("Não deve bloquear com poucas falhas")
    void shouldNotLock_WithFewFailures() {
        for (int i = 0; i < 3; i++) {
            loginRateLimiter.onAuthenticationFailure("user@email.com");
        }

        assertFalse(loginRateLimiter.isLocked("user@email.com"));
        assertTrue(loginRateLimiter.tryConsume("127.0.0.1", "user@email.com"));
    }

    @Test
    @DisplayName("Sucesso deve limpar o lockout")
    void success_ShouldClearLockout() {
        for (int i = 0; i < 5; i++) {
            loginRateLimiter.onAuthenticationFailure("user@email.com");
        }
        assertTrue(loginRateLimiter.isLocked("user@email.com"));

        loginRateLimiter.onAuthenticationSuccess("user@email.com");

        assertFalse(loginRateLimiter.isLocked("user@email.com"));
    }

    @Test
    @DisplayName("Bucket deve permitir até o limite por janela")
    void tryConsume_ShouldAllowUpToLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(loginRateLimiter.tryConsume("127.0.0.1", "user@email.com"));
        }

        assertFalse(loginRateLimiter.tryConsume("127.0.0.1", "user@email.com"));
    }
}
