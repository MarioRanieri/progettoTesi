package com.example.auth_service1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import org.springframework.http.HttpStatus;

/**
 * Configura le impostazioni di sicurezza dell'applicazione,
 * inclusa l'autenticazione, l'autorizzazione e la gestione delle sessioni.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Definisce il bean per l'encoder delle password utilizzando BCrypt.
     *
     * @return Un'istanza di {@link PasswordEncoder} basata su BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la catena di filtri di sicurezza per gestire le richieste HTTP,
     * definendo le regole di accesso e le politiche di sessione.
     *
     * @param http L'istanza di {@link HttpSecurity} da configurare.
     * @return Il {@link SecurityFilterChain} configurato.
     * @throws Exception Se si verifica un errore nella configurazione.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disabilita la protezione CSRF poiché l'app utilizza sessioni stateless
            .csrf(csrf -> csrf.disable())

            // Configura le autorizzazioni delle richieste HTTP
            .authorizeHttpRequests(authz -> authz
                // Gli endpoint di registrazione, login e JWKS sono pubblici
                .requestMatchers("/auth/register", "/auth/login", "/oauth2/jwks").permitAll()
                // L'endpoint di eliminazione richiede autenticazione
                .requestMatchers("/auth/delete").authenticated()
                // Ogni altra richiesta richiede autenticazione
                .anyRequest().authenticated()
            )

            // Disabilita il form di login in quanto utilizziamo token JWT
            .formLogin(form -> form.disable())

            // Disabilita il logout gestito da Spring Security
            .logout(logout -> logout.disable())

            // Configura la gestione delle sessioni come stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Gestione delle eccezioni di autenticazione e autorizzazione
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Imposta il codice di stato 403 FORBIDDEN
                    response.sendError(HttpStatus.FORBIDDEN.value(), "Accesso negato");
                })
            );

        return http.build();
    }
}