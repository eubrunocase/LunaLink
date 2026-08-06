package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import com.LunaLink.application.web.dto.SecurityDTO.RefreshResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final TokenRevocationService tokenRevocationService;
    private final UserRepositoryPort userRepositoryPort;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 TokenService tokenService,
                                 RefreshTokenService refreshTokenService,
                                 TokenRevocationService tokenRevocationService,
                                 UserRepositoryPort userRepositoryPort) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.tokenRevocationService = tokenRevocationService;
        this.userRepositoryPort = userRepositoryPort;
    }

    public LoginResponseDTO authenticate(AuthenticationDTO data) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);
        Users user = (Users) auth.getPrincipal();
        log.debug("Autenticação bem-sucedida para: {}", data.email());
        return buildLoginResponse(user);
    }

    public RefreshResponseDTO refresh(String rawRefreshToken) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(rawRefreshToken);
        Users user = userRepositoryPort.findById(rotated.userId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Usuário não encontrado"));
        return buildRefreshResponse(user, rotated.refreshToken());
    }

    public void logout(String rawRefreshToken, String accessToken) {
        refreshTokenService.revoke(rawRefreshToken);
        tokenRevocationService.blacklistAccessToken(accessToken);
    }

    private LoginResponseDTO buildLoginResponse(Users user) {
        String accessToken = tokenService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new LoginResponseDTO(accessToken, refreshToken, TokenService.ACCESS_TOKEN_TTL_SECONDS, "Bearer");
    }

    private RefreshResponseDTO buildRefreshResponse(Users user, String refreshToken) {
        String accessToken = tokenService.generateToken(user);
        return new RefreshResponseDTO(accessToken, refreshToken, TokenService.ACCESS_TOKEN_TTL_SECONDS, "Bearer");
    }
}
