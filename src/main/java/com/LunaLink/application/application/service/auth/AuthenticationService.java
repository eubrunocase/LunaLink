package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthenticationService(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public String authenticate(AuthenticationDTO data) {
        System.out.println("Tentando autenticar usuário: " + data.email());
        try {
            UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            Authentication auth = this.authenticationManager.authenticate(usernamePassword);

            System.out.println("Autenticação bem-sucedida para: " + data.email());
            Users user = (Users) auth.getPrincipal();
            return tokenService.generateToken(user);
        } catch (AuthenticationException e) {
            System.err.println("Falha na autenticação: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Erro inesperado na autenticação: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro na autenticação", e);
        }
    }
}
