package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.auth.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepositoryPort userRepositoryPort;


    public SecurityFilter(TokenService tokenService, UserRepositoryPort userRepositoryPort) {
        this.tokenService = tokenService;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            var token = this.recoverToken(request);

            System.out.println("=== DEBUG REQUEST ===");
            System.out.println("Método: " + request.getMethod());
            System.out.println("URL: " + request.getRequestURL());

            if (token != null) {
                System.out.println("Token encontrado: " + token.substring(0, Math.min(10, token.length())) + "...");
                var login = tokenService.validateToken(token);

                if (!"Invalid token".equals(login)) {
                    System.out.println("Token válido para usuário: " + login);

                    UserDetails user = userRepositoryPort.findByLogin(login);

                    if (user == null) {
                        throw new ServletException("ERRO NO MÉTODO doFilterInternal do SecurityFilter: User not found");
                    }

                    if (user != null) {
                        System.out.println("Configurando autenticação para: " + login + " com roles: " + user.getAuthorities());
                        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        System.out.println("Usuário não encontrado para o login: " + login);
                    }
                } else {
                    System.out.println("Token inválido");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro no filtro de segurança: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

   public String recoverToken(HttpServletRequest request) {
       var authHeader = request.getHeader("Authorization");
       if(authHeader == null) return null;
       return authHeader.replace("Bearer ", "");
   }

}




