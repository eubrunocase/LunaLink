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

                        .requestMatchers( "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html").permitAll()

                        // ================= Actuator =================
                        .requestMatchers("/actuator/prometheus").permitAll()

                        // ================= Autenticação =================
                        .requestMatchers(HttpMethod.POST,"/lunaLink/auth/login").permitAll()

                        // ================= Disponibilidade de espaços =================
                        .requestMatchers(HttpMethod.GET, "/lunaLink/availabilitySpaces/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/lunaLink/availabilitySpaces/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/lunaLink/availabilitySpaces/**").permitAll()

                        // ================= Encomendas=================
                        .requestMatchers(HttpMethod.GET, "/lunaLink/delivery/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lunaLink/delivery/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/lunaLink/delivery/**").authenticated()

                        // ================= Administrador =================
                        .requestMatchers(HttpMethod.GET,"/lunaLink/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/lunaLink/users/**").permitAll()
                        .requestMatchers(HttpMethod.PUT,"/lunaLink/users/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE,"/lunaLink/users/**").permitAll()

                        // ================= Reserva =================
                        .requestMatchers(HttpMethod.POST,"/lunaLink/reservation").permitAll()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation").permitAll()
                        .requestMatchers(HttpMethod.DELETE,"/lunaLink/reservation/**").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.DELETE,"/lunaLink/reservation/checkAvaliability/**").permitAll()

                        // ================= Espaço =================
                        .requestMatchers(HttpMethod.POST,"/lunaLink/space/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/space/**").permitAll()

                        // ================= Reserva Mensal =================
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservaMensal/**").hasRole("ADMIN_ROLE")

                        // ================= Check-in Ginásio =================
                        .requestMatchers(HttpMethod.POST,"/lunaLink/checkInGym/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/checkInGym/**").hasRole("ADMIN_ROLE")

                        // ================= Check-out Ginásio =================
                        .requestMatchers(HttpMethod.GET,"/lunaLink/checkOutGym/**").permitAll()


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

                UserDetails residentUser = userRepositoryPort.findByLogin(username);
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
