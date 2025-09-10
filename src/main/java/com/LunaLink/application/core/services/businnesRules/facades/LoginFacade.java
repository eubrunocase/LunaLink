package com.LunaLink.application.core.services.businnesRules.facades;

import com.LunaLink.application.core.domain.Users;
import com.LunaLink.application.core.services.businnesRules.AdministratorService;
import com.LunaLink.application.core.services.businnesRules.ResidentService;
import com.LunaLink.application.core.services.jwtService.TokenService;
import com.LunaLink.application.web.dto.SecurityDTOs.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTOs.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class LoginFacade {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final ResidentService residentService;
    private final AdministratorService administratorService;

    public LoginFacade(AuthenticationManager authenticationManager,
                       TokenService tokenService,
                       ResidentService residentService,
                       AdministratorService administratorService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.residentService = residentService;
        this.administratorService = administratorService;
    }

    public ResponseEntity<LoginResponseDTO> login (AuthenticationDTO data) {
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
