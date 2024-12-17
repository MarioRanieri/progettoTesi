package com.example.app_service2.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.app_service2.model.User;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());

    @Value("${jwt.secret}")
    private String secret;

    private Set<String> invalidatedTokens = new HashSet<>();

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

    public boolean validateToken(String token, User user) {
    LOGGER.info("\nin app-service2/JwtUtil,\n Validating\n token: " + token);
    try {
        Claims claims = getClaims(token);
        String username = claims.getSubject();
        // Controlla se il token è scaduto
        boolean isTokenExpired = isTokenExpired(claims);
        if (isTokenExpired) {
            LOGGER.warning("Token expired");
            return false;
        }
        // Controlla se il token è stato invalidato
        boolean isTokenInvalidated = isTokenInvalidated(token);
        if (isTokenInvalidated) {
            LOGGER.warning("Token invalidated");
            return false;
        }
        // Verifica che il token appartenga all'utente corretto
        boolean isUsernameValid = username.equals(user.getUsername());
        if (!isUsernameValid) {
            LOGGER.warning("Token does not belong to the user");
            return false;
        }
        return true;
    } catch (Exception e) {
        LOGGER.severe("\nin app-service2/JwtUtil,\n Token\n validation failed: " + e.getMessage());
        return false;
    }
}

    private boolean isTokenInvalidated(String token) {
    return invalidatedTokens.contains(token);
}
    // Metodo per invalidare un token 
    public void invalidateToken(String token) { 
        invalidatedTokens.add(token); 
        LOGGER.info("Token invalidato: " + token); 
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
    if (authorities instanceof List<?>) {
        return ((List<Map<String, String>>) authorities).stream()
            .map(map -> map.get("authority").replace("ROLE_ROLE", "ROLE_"))
            .collect(Collectors.toSet());
    } else {
        return new HashSet<>();
    }
}

}




