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

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.security.interfaces.RSAPublicKey;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Configurazione di sicurezza per l'applicazione.
 * <p>
 * Questa classe configura la sicurezza dell'applicazione, definendo le regole di accesso,
 * la gestione dei token JWT e l'integrazione con il server di autorizzazione.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String JWKS_URI = "http://localhost:8081/oauth2/jwks";
    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class.getName());

    /**
     * Configura la catena di filtri di sicurezza per l'applicazione.
     *
     * @param http      l'istanza di {@link HttpSecurity} da configurare.
     * @param jwtFilter il filtro JWT personalizzato.
     * @return una {@link SecurityFilterChain} configurata.
     * @throws Exception se si verifica un errore durante la configurazione.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Endpoint pubblici
                .requestMatchers("/app-service2/check-username", "/app-service2/validate-user", "/app-service2/register").permitAll()
                // Rimuoviamo la gestione dei ruoli
                // Tutte le altre richieste richiedono autenticazione
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    // Rimuoviamo l'uso del JwtAuthenticationConverter poiché non gestiamo le authorities
                    // .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Fornisce un {@link PasswordEncoder} utilizzando l'algoritmo BCrypt.
     *
     * @return un'istanza di {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura il decoder JWT utilizzando la chiave pubblica ottenuta dal JWKS.
     *
     * @return un'istanza di {@link JwtDecoder} configurata con la chiave pubblica.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
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

    /**
     * Recupera il JWKS dall'URI specificato.
     *
     * @return una mappa contenente il JWKS.
     * @throws Exception se si verifica un errore durante il recupero o la lettura del JWKS.
     */
    private Map<String, Object> fetchJwks() throws Exception {
        HttpResponse<String> response = Unirest.get(JWKS_URI).asString();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Errore nel recupero del JWKS: status " + response.getStatus());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        return objectMapper.readValue(response.getBody(), typeRef);
    }

    /**
     * Estrae la chiave pubblica dal JWKS.
     *
     * @param jwks la mappa contenente il JWKS.
     * @return la chiave pubblica RSA estratta.
     * @throws Exception se si verifica un errore durante l'estrazione della chiave pubblica.
     */
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
        LOGGER.info("Chiave pubblica generata con successo");
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }

    // Rimosso il bean jwtAuthenticationConverter() poiché non gestiamo le authorities
}
