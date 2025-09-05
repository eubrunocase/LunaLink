package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.AdministratorService;
import com.LunaLink.application.core.services.businnesRules.ResidentService;
import com.LunaLink.application.core.services.businnesRules.facades.LoginFacade;
import com.LunaLink.application.core.services.jwtService.TokenService;
import com.LunaLink.application.core.domain.Users;
import com.LunaLink.application.web.dto.SecurityDTOs.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTOs.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lunaLink/auth")
public class AuthenticationController {

       private final LoginFacade loginFacade;

       public AuthenticationController(LoginFacade loginFacade) {
           this.loginFacade = loginFacade;
       }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid AuthenticationDTO data) {
        ResponseEntity<LoginResponseDTO> facadeResponse = loginFacade.login(data);

        if (facadeResponse.getStatusCode().is2xxSuccessful() && facadeResponse.getBody() != null) {
            String jwt = facadeResponse.getBody().token();
            return ResponseEntity.ok(jwt);
        }

        return ResponseEntity.badRequest().build();
    }


}
