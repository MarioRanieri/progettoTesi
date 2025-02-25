package com.example.auth_service1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Configurazione per la gestione della coppia di chiavi RSA utilizzata per la firma e la verifica dei token JWT.
 */
@Configuration
public class KeyConfig {

    /**
     * Crea un bean per la coppia di chiavi RSA.
     *
     * @return La coppia di chiavi RSA.
     * @throws NoSuchAlgorithmException Se l'algoritmo RSA non è disponibile.
     */
    @Bean
    public KeyPair keyPair() throws NoSuchAlgorithmException {
        // Genera una nuova coppia di chiavi RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Lunghezza della chiave RSA
        return keyPairGenerator.generateKeyPair();
    }
}
