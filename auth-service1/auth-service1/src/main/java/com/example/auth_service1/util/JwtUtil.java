package com.example.auth_service1.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.KeyPair;
import java.util.Date;

/**
 * Utility per la generazione e la validazione dei token JWT.
 */
@Component
public class JwtUtil {

    private final PrivateKey privateKey;

    /**
     * Costruttore che riceve la chiave privata tramite iniezione delle dipendenze.
     *
     * @param keyPair La coppia di chiavi RSA.
     */
    public JwtUtil(KeyPair keyPair) {
        this.privateKey = keyPair.getPrivate();
    }

    /**
     * Genera un token JWT per un dato nome utente.
     *
     * @param username Il nome utente per il quale generare il token.
     * @return Il token JWT generato.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000 * 60 * 60); // 1 ora di validità

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
