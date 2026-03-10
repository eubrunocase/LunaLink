package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final SecurityFilter securityFilter;

    public SecurityConfiguration(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        // ================= Públicos (Infraestrutura e Login) =================
                        .requestMatchers( "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.POST,"/lunaLink/auth/login").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/ws-lunalink").permitAll() // WebSocket Handshake

                        // ================= Administrador (Gestão de Usuários) =================
                        // Apenas Admin pode criar, editar ou deletar usuários
                        .requestMatchers(HttpMethod.POST,"/lunaLink/users/**").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.PUT,"/lunaLink/users/**").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.DELETE,"/lunaLink/users/**").hasRole("ADMIN_ROLE")
                        
                        // ================= Autenticados (Moradores e Admins) =================
                        
                        // Usuários (Leitura para User Summary e Perfil)
                        .requestMatchers(HttpMethod.GET,"/lunaLink/users/**").authenticated()

                        // Disponibilidade de espaços
                        .requestMatchers("/lunaLink/availabilitySpaces/**").authenticated()

                        // Encomendas
                        .requestMatchers("/lunaLink/delivery/**").authenticated()

                        // Reservas (Criação e Leitura)
                        .requestMatchers(HttpMethod.POST,"/lunaLink/reservation").authenticated()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/checkAvaliability/**").authenticated()
                        
                        // Reservas (Ações Administrativas Específicas)
                        .requestMatchers(HttpMethod.DELETE,"/lunaLink/reservation/**").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.PUT,"/lunaLink/reservation/{id}/approve").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.PUT,"/lunaLink/reservation/{id}/reject").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/report/**").hasRole("ADMIN_ROLE")

                        // Espaços (Leitura)
                        .requestMatchers(HttpMethod.GET,"/lunaLink/space/**").authenticated()
                        // Espaços (Escrita - Apenas Admin deveria criar espaços, se houver endpoint)
                        .requestMatchers(HttpMethod.POST,"/lunaLink/space/**").hasRole("ADMIN_ROLE")

                        // Qualquer outra requisição deve estar autenticada
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
            UserRepositoryPort userRepositoryPort) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

                UserDetails residentUser = userRepositoryPort.findByEmail(username);
                if (residentUser != null) {
                    System.out.println("Usuário encontrado: " + username);
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


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
