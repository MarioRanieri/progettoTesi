package com.example.app_service2.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

@Component
public class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());

    @Value("${jwt.secret}")
    private String secret;

    public SecretKey getKey() {
        if (secret == null) {
            LOGGER.severe("\nin app-service2/JwtUtil,\n Chiave segreta non trovata");
            throw new RuntimeException("\n in app-service2/JwtUtil,\n Chiave segreta non trovata");
        }
        LOGGER.info("\nin app-service2/JwtUtil,\n Chiave segreta trovata: " + secret);
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public Claims getClaims(String token) {
        System.out.println("SONO QUI");
        LOGGER.info("\nin app-service2/JwtUtil,\n Getting claims for\n token: " + token);
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        LOGGER.info("\nin app-service2/JwtUtil,\n Validating\n token: " + token);
        try {
            Claims claims = getClaims(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            LOGGER.severe("\nin app-service2/JwtUtil,\n Token\n validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        boolean expired = claims.getExpiration().before(new Date());
        LOGGER.info("\nin app-service2/JwtUtil,\n Is token\n expired: " + expired);
        return expired;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);
        Object authorities = claims.get("authorities");
        if (authorities instanceof Set<?>) {
            LOGGER.info("\nin app-service2/JwtUtil,\n Authorities from token: " + authorities);
            return (Set<String>) authorities;
        } else {
            LOGGER.warning("\nin app-service2/JwtUtil,\n No authorities found in token");
            return new HashSet<>();
        }
    }
}




