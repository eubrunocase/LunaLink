package com.LunaLink.application.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public interface UserAuthenticationProvider {
    UserDetails findByLogin(String login);
}
