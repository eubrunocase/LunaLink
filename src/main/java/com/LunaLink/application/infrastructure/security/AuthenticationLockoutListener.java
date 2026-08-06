package com.LunaLink.application.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationLockoutListener {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationLockoutListener.class);

    private final LoginRateLimiter loginRateLimiter;

    public AuthenticationLockoutListener(LoginRateLimiter loginRateLimiter) {
        this.loginRateLimiter = loginRateLimiter;
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName();
        loginRateLimiter.onAuthenticationFailure(email);
        log.debug("Falha de autenticação registrada para: {}", email);
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();
        loginRateLimiter.onAuthenticationSuccess(email);
    }
}
