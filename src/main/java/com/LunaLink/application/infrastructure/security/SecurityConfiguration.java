package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final SecurityFilter securityFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfiguration(SecurityFilter securityFilter,
                                 RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.securityFilter = securityFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(restAuthenticationEntryPoint))
                .authorizeHttpRequests(authorize -> authorize

                        // ================= Públicos (Infraestrutura e Login) =================
                        .requestMatchers( "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.POST,"/lunaLink/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/lunaLink/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST,"/lunaLink/auth/logout").permitAll()
                        // WebSocket (SockJS): usa PathPatternRequestMatcher (match por path) porque o
                        // MvcRequestMatcher não casa requisições de upgrade WebSocket (caem em
                        // anyRequest().authenticated() e geram AuthorizationDeniedException no log + fallback do SockJS)
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/ws-lunalink/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/ws-lunalink")).permitAll() // Handshake (GET/PUT)
                        // Push Subscription (Requer login, qualquer role)
                        .requestMatchers("/lunaLink/push/**").authenticated()

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
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/checkAvaliability/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/findByUser/**").authenticated()

                        // Reservas (Ações Administrativas Específicas)
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/pending-inspection").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/report/**").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.POST,"/lunaLink/reservation/report/**").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.DELETE,"/lunaLink/reservation/**").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.PUT,"/lunaLink/reservation/{id}/approve").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.PUT,"/lunaLink/reservation/{id}/reject").hasRole("ADMIN_ROLE")
                        .requestMatchers(HttpMethod.GET,"/lunaLink/reservation/**").authenticated()

                        // Espaços (Leitura)
                        .requestMatchers(HttpMethod.GET,"/lunaLink/space/**").authenticated()
                        // Espaços (Escrita - Apenas Admin deveria criar espaços, se houver endpoint)
                        .requestMatchers(HttpMethod.POST,"/lunaLink/space/**").hasRole("ADMIN_ROLE")

                        // ================= Equipamentos (US-04) =================
                        // Morador cria reserva (self-service); Admin/Funcionário também podem registrar em nome do morador
                        .requestMatchers(HttpMethod.POST, "/lunaLink/equipment-reservation").authenticated()
                        // Morador visualiza as próprias reservas de equipamento
                        .requestMatchers(HttpMethod.GET, "/lunaLink/equipment-reservation/mine").authenticated()
                        // Morador cancela a própria reserva; Admin/Funcionário cancelam por gestão (ordem importa: matcher específico antes do genérico)
                        .requestMatchers(HttpMethod.PATCH, "/lunaLink/equipment-reservation/{id}/cancel").authenticated()
                        // Apenas Admin/Funcionário faz check-in/check-out (handover/return) e lista
                        .requestMatchers(HttpMethod.PATCH, "/lunaLink/equipment-reservation/**").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/lunaLink/equipment-reservation/**").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
                        
                        // ================= Ocorrências (US-03) =================
                        // Abertura de ocorrência: restrita ao morador
                        .requestMatchers(HttpMethod.POST, "/lunaLink/occurrences").authenticated()
                        .requestMatchers(HttpMethod.GET, "/lunaLink/occurrences").authenticated()
                        .requestMatchers(HttpMethod.GET, "/lunaLink/occurrences/{uuid}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/lunaLink/occurrences/{uuid}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/lunaLink/occurrences/{uuid}").authenticated()

                        // ================= US-05: Vistoria, Termo de Responsabilidade e Convidados =================
                        // Vistoria (pré/pós-evento): apenas Funcionário
                        .requestMatchers(HttpMethod.POST, "/lunaLink/reservations/*/inspection").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
                        // Termo de Responsabilidade: apenas morador (dono da reserva)
                        .requestMatchers(HttpMethod.POST, "/lunaLink/reservations/*/liability-term/sign").authenticated()
                        // Convidados: consulta (Funcionário/Admin) e check-in (Funcionário)
                        .requestMatchers(HttpMethod.GET, "/lunaLink/reservations/*/guests").hasAnyRole("ADMIN_ROLE", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/lunaLink/reservations/*/guests/*/check-in").hasRole("EMPLOYEE")

                        // Qualquer outra requisição deve estar autenticada
                        .anyRequest().authenticated()
                )
                .addFilterAfter(securityFilter, ExceptionTranslationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AuthenticationEventPublisher eventPublisher) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setAuthenticationEventPublisher(eventPublisher);
        return providerManager;
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserRepositoryPort userRepositoryPort) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

                UserDetails residentUser = userRepositoryPort.findByEmail(username);
                if (residentUser != null) {
                    log.debug("Usuário encontrado: {}", username);
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
