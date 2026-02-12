package com.LunaLink.application.application.service.auth;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationUserService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;

    public AuthorizationUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepositoryPort.findByLogin(username);
    }

}
