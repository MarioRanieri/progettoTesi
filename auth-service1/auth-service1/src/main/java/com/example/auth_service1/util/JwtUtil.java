package com.example.auth_service1.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

// Nessun metodo di validazione o analisi del token, qui viene solo generato
@Component
public class JwtUtil {

    @Value("${jwt.secret}") 
    private String secret;

    // Genera una SecretKey dalla chiave segreta 
    private SecretKey getKey() {
        System.out.println("prendo la chiave dall'application.properties in auth-service1/JwtUtil: " + secret);
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String generateToken(String username, Set<String> authorities) {
    Map<String, Object> claims = new HashMap<>();
    List<Map<String, String>> authoritiesList = authorities.stream()
            .map(authority -> {
                Map<String, String> map = new HashMap<>();
                map.put("authority", authority.startsWith("ROLE_") ? authority : "ROLE_" + authority);
                return map;
            })
            .collect(Collectors.toList());

    claims.put("authorities", authoritiesList);
    System.out.println("\n ora genero il token in auth-service1/JwtUtil: ");
    return createToken(claims, username);
}


    private String createToken(Map<String, Object> claims, String subject) {
        System.out.println("ora creo il token in auth-service1/JwtUtil: ");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 ora
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}


