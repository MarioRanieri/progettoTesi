package com.example.app_service2.config;

import com.example.app_service2.filter.JwtFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String JWKS_URI = "http://localhost:8081/oauth2/jwks";
    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class.getName());

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/app-service2/check-username", "/app-service2/validate-user", "/app-service2/register").permitAll()
                .requestMatchers("/app-service2/admin-endpoint").hasRole("ADMIN")
                .requestMatchers("/app-service2/user-endpoint").hasRole("USER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); 
            return http.build(); 
        }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<Map<String, String>> authorities = jwt.getClaim("authorities");
            return authorities.stream()
                .map(auth -> new SimpleGrantedAuthority(auth.get("authority")))
                .collect(Collectors.toList());
        });
        return converter;
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public NimbusJwtDecoder jwtDecoder() {
        try {
            LOGGER.info("Inizio il recupero del JWKS");
            Map<String, Object> jwks = fetchJwks();
            LOGGER.info("JWKS recuperato con successo");
            RSAPublicKey publicKey = getPublicKeyFromJwks(jwks);
            LOGGER.info("Chiave pubblica estratta con successo");
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception e) {
            LOGGER.severe("Errore durante la configurazione del decoder JWT: " + e.getMessage());
            throw new RuntimeException("Errore durante la configurazione del decoder JWT", e);
        }
    }

    private Map<String, Object> fetchJwks() throws Exception {
        HttpResponse<String> response = Unirest.get(JWKS_URI).asString();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Errore nel recupero del JWKS: status " + response.getStatus());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        return objectMapper.readValue(response.getBody(), typeRef);
    }

    private RSAPublicKey getPublicKeyFromJwks(Map<String, Object> jwks) throws Exception {
        if (jwks == null || !jwks.containsKey("keys")) {
            throw new RuntimeException("Il JWKS non contiene il campo 'keys'");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> keys = objectMapper.convertValue(jwks.get("keys"), new TypeReference<List<Map<String, Object>>>() {});
        if (keys == null || keys.isEmpty()) {
            throw new RuntimeException("La lista 'keys' è vuota o null");
        }
        Map<String, Object> key = keys.get(0);
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode((String) key.get("n")));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode((String) key.get("e")));
        System.out.println("chiave pubblica generata!");
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }
}

