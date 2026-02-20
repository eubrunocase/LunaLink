package com.LunaLink.application.application.facades.auth;

import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.application.service.auth.TokenService;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class LoginFacade {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserServicePort userService;

    public LoginFacade(AuthenticationManager authenticationManager,
                       TokenService tokenService,
                       UserServicePort userService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    public ResponseEntity<LoginResponseDTO> login(AuthenticationDTO data) {
        try {
            System.out.println("recebendo dados de login para " + data.login());
            Authentication request = UsernamePasswordAuthenticationToken.unauthenticated(data.login(), data.password());
            Authentication response = this.authenticationManager.authenticate(request);

            var token = tokenService.generateToken((Users) response.getPrincipal());

            return ResponseEntity.ok(new LoginResponseDTO(token));

        } catch (Exception e) {
            System.err.println("Erro no login para: " + data.login());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}
