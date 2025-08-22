package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.infrastructure.repository.administrator.AdministratorRepository;
import com.LunaLink.application.infrastructure.repository.resident.ResidentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final SecurityFilter securityFilter;

    public SecurityConfiguration(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers( "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html").permitAll()

                        .requestMatchers(HttpMethod.POST,"/lunaLink/auth/login").permitAll()

                        .requestMatchers(HttpMethod.GET,"/lunaLink/adm/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST,"/lunaLink/adm/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET,"/lunaLink/resident").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST,"/lunaLink/resident").permitAll()


                        .requestMatchers(HttpMethod.POST,"/lunaLink/reservation").permitAll()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation").permitAll()

                        .requestMatchers(HttpMethod.POST,"/lunaLink/space/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/space/**").permitAll()

                        .requestMatchers(HttpMethod.GET,"/lunaLink/monthlyReservation/**").permitAll()


                        .anyRequest().authenticated()
                ) .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public UserDetailsService userDetailsService(
            AdministratorRepository administratorRepository,
            ResidentRepository residentRepository) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                UserDetails adminUser = administratorRepository.findByLogin(username);
                if (adminUser != null) {
                    System.out.println("Usuário administrador encontrado: " + username);
                    return adminUser;
                }

                UserDetails residentUser = residentRepository.findByLogin(username);
                if (residentUser != null) {
                    System.out.println("Usuário professor encontrado: " + username);
                    return residentUser;
                }

                throw new UsernameNotFoundException("Usuário não encontrado: " + username);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
