package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.TokenBlacklistRepositoryPort;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

@Service
public class TokenRevocationService {

    private final TokenService tokenService;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    public TokenRevocationService(TokenService tokenService,
                                  TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort) {
        this.tokenService = tokenService;
        this.tokenBlacklistRepositoryPort = tokenBlacklistRepositoryPort;
    }

    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null) {
            return;
        }
        try {
            DecodedJWT decoded = tokenService.validateToken(accessToken);
            String jti = decoded.getId();
            if (jti != null) {
                tokenBlacklistRepositoryPort.blacklist(jti, decoded.getExpiresAtAsInstant());
            }
        } catch (JWTVerificationException exception) {
            // token já inválido/expirado — nada a blacklistar
        }
    }
}
