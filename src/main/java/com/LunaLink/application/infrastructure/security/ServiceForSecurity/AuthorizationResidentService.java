package com.LunaLink.application.infrastructure.security.ServiceForSecurity;

import com.LunaLink.application.infrastructure.repository.ResidentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationResidentService implements UserDetailsService {

    private final ResidentRepository residentRepository;

    public AuthorizationResidentService(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return residentRepository.findByLogin(username);
    }
}
