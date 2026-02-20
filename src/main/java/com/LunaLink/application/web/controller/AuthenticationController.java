package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.auth.LoginFacade;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
