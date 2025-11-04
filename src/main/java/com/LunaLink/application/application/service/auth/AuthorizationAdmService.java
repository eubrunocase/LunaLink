package com.LunaLink.application.application.service.auth;


import com.LunaLink.application.infrastructure.repository.administrator.AdministratorRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationAdmService implements UserDetailsService{

    private final AdministratorRepository administratorRepository;

    public AuthorizationAdmService(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return administratorRepository.findByLogin(username);
    }
}
