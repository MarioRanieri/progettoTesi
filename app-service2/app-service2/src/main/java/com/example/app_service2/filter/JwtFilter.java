package com.example.app_service2.filter;

import com.example.app_service2.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(JwtFilter.class.getName());

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        LOGGER.info("Authorization Header: " + authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            LOGGER.info("JWT: " + jwt);
            try {
                Claims claims = jwtUtil.getClaims(jwt);
                String username = claims.getSubject();
                LOGGER.info("Username from Claims: " + username);

                if (username != null && jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Set<String> roles = jwtUtil.getRolesFromToken(jwt).stream()
                        .map(role -> "ROLE_" + role)
                        .collect(Collectors.toSet());
                    LOGGER.info("Roles from Token: " + roles);
                    UserDetails userDetails = new User(username, "", roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                    // Salva il token nel contesto di sicurezza
                    LOGGER.info("Token JWT salvato nel contesto di sicurezza per l'utente: " + username);
                }
            } catch (ExpiredJwtException | MalformedJwtException | SecurityException | UnsupportedJwtException e) {
                LOGGER.severe("Invalid token: " + e.getMessage());
                throw new ServletException("Invalid token.", e);
            } catch (IllegalArgumentException e) {
                LOGGER.severe("Token claims string is empty: " + e.getMessage());
                throw new ServletException("Token claims string is empty.", e);
            }
        } else {
            LOGGER.warning("JWT is invalid or Authorization header is missing");
        }

        chain.doFilter(request, response);
    }
}




