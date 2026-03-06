package com.LunaLink.application.application.facades.auth;

import com.LunaLink.application.application.service.auth.AuthenticationService;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class LoginFacade {

    private final AuthenticationService authenticationService;

    public LoginFacade(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public ResponseEntity<LoginResponseDTO> login(AuthenticationDTO data) {
        try {
            String token = authenticationService.authenticate(data);
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
