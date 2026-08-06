package com.LunaLink.application.application.facades.auth;

import com.LunaLink.application.application.service.auth.AuthenticationService;
import com.LunaLink.application.infrastructure.security.LoginRateLimiter;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class LoginFacade {

    private static final Logger log = LoggerFactory.getLogger(LoginFacade.class);

    private final AuthenticationService authenticationService;
    private final LoginRateLimiter loginRateLimiter;

    public LoginFacade(AuthenticationService authenticationService,
                       LoginRateLimiter loginRateLimiter) {
        this.authenticationService = authenticationService;
        this.loginRateLimiter = loginRateLimiter;
    }

    public ResponseEntity<LoginResponseDTO> login(AuthenticationDTO data, String clientIp) {
        if (loginRateLimiter.isLocked(data.email())) {
            log.warn("Login bloqueado por lockout: {}", data.email());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        if (!loginRateLimiter.tryConsume(clientIp, data.email())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {
            LoginResponseDTO response = authenticationService.authenticate(data);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException exception) {
            log.debug("Falha na autenticação para: {}", data.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
