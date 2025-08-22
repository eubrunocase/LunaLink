package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.businnesRules.AdministratorService;
import com.LunaLink.application.application.businnesRules.ResidentService;
import com.LunaLink.application.application.jwtService.TokenService;
import com.LunaLink.application.core.Users;
import com.LunaLink.application.web.dto.SecurityDTOs.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTOs.LoginResponseDTO;
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

       private final AuthenticationManager authenticationManager;
       private final TokenService tokenService;
       private final ResidentService residentService;
       private final AdministratorService administratorService;

       public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService, ResidentService residentService, AdministratorService administratorService) {
           this.authenticationManager = authenticationManager;
           this.tokenService = tokenService;
           this.residentService = residentService;
           this.administratorService = administratorService;
       }

       @PostMapping("/login")
       public ResponseEntity<LoginResponseDTO> login (@RequestBody AuthenticationDTO data) {
           try {
               System.out.println("recebendo dados de login para " + data.login());
               Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(data.login(), data.password());
               Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
               var token = tokenService.generateToken((Users) authenticationResponse.getPrincipal());
               return ResponseEntity.ok(new LoginResponseDTO(token));
           } catch (Exception e) {
               System.err.println("Erro no login para: " + data.login());
               e.printStackTrace();
               return ResponseEntity.badRequest().build();
           }
       }


}
