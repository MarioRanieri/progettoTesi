package com.example.auth_service1.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;

// Nessun metodo di validazione o analisi del token, qui viene solo generato
@Component
public class JwtUtil {

    @Value("${jwt.secret}") 
    private String secret;

    // Genera una SecretKey dalla chiave segreta 
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String generateToken(String username, Set<String> authorities) {
        Map<String, Object> claims=new HashMap<>();
        claims.put("authorities", authorities);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 ora
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}


