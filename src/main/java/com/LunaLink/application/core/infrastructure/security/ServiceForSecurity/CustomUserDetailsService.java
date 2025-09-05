package com.LunaLink.application.core.infrastructure.security.ServiceForSecurity;

import com.LunaLink.application.core.infrastructure.persistence.administrator.AdministratorRepository;
import com.LunaLink.application.core.infrastructure.persistence.resident.ResidentRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final ResidentRepository residentRepository;
    private final AdministratorRepository administratorRepository;

    public CustomUserDetailsService(ResidentRepository residentRepository, AdministratorRepository administratorRepository) {
        this.residentRepository = residentRepository;
        this.administratorRepository = administratorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userAdm = administratorRepository.findByLogin(username);
        if (userAdm != null) {
            return userAdm;
        }

        UserDetails userResident = residentRepository.findByLogin(username);
        if (userResident != null) {
            return userResident;
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }
}
