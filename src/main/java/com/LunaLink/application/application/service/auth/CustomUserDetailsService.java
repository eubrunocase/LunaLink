package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;

    public CustomUserDetailsService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDetails userResident = userRepositoryPort.findByLogin(username);
        if (userResident != null) {
            return userResident;
        }
        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }

}
