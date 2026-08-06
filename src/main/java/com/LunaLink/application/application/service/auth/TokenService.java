package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.domain.model.users.Users;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenService {

    public static final String AUDIENCE = "lunalink-api";
    public static final long ACCESS_TOKEN_TTL_SECONDS = 2 * 60 * 60;

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Users user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Instant now = Instant.now();
            return JWT.create()
                    .withIssuer("User")
                    .withSubject(user.getEmail())
                    .withAudience(AUDIENCE)
                    .withIssuedAt(now)
                    .withNotBefore(now)
                    .withExpiresAt(now.plus(ACCESS_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS))
                    .withJWTId(UUID.randomUUID().toString())
                    .withClaim("roles", user.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .collect(Collectors.toList()))
                    .withClaim("token_version", user.getTokenVersion())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public DecodedJWT validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("User")
                .withAudience(AUDIENCE)
                .build()
                .verify(token);
    }

    public String getSubject(String token) {
        return validateToken(token).getSubject();
    }
}
