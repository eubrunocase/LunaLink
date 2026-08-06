package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.application.ports.output.TokenBlacklistRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.auth.TokenService;
import com.LunaLink.application.domain.model.users.Users;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TokenAuthenticator {

    private final TokenService tokenService;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    public TokenAuthenticator(TokenService tokenService,
                              UserRepositoryPort userRepositoryPort,
                              TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort) {
        this.tokenService = tokenService;
        this.userRepositoryPort = userRepositoryPort;
        this.tokenBlacklistRepositoryPort = tokenBlacklistRepositoryPort;
    }

    public Users authenticate(String token) {
        DecodedJWT decoded;
        try {
            decoded = tokenService.validateToken(token);
        } catch (JWTVerificationException exception) {
            throw new BadCredentialsException("Token inválido ou expirado", exception);
        }

        String jti = decoded.getId();
        if (jti != null && tokenBlacklistRepositoryPort.isBlacklisted(jti)) {
            throw new BadCredentialsException("Token revogado");
        }

        Users user = userRepositoryPort.findByEmail(decoded.getSubject());
        if (user == null) {
            throw new BadCredentialsException("Usuário não encontrado");
        }

        Integer tokenVersion = decoded.getClaim("token_version").asInt();
        if (!Objects.equals(user.getTokenVersion(), tokenVersion)) {
            throw new BadCredentialsException("Token desatualizado");
        }

        return user;
    }
}
