package com.example.auth_service1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabilita CSRF
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/auth/register", "/auth/login", "/oauth2/jwks").permitAll() // Endpoint pubblici
                .requestMatchers("/auth/delete").permitAll() // Permetti l'eliminazione senza autenticazione (solo per test)
                .anyRequest().authenticated() // Tutti gli altri endpoint richiedono autenticazione
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            ) // Imposta la pagina di login
            .logout(logout -> logout
                .permitAll()
            ) // Permetti il logout
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            ); // Configura le sessioni come stateless

        return http.build();
    }
}






