package com.example.auth_service1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class JwkSetController {

    private final KeyPair keyPair;

    public JwkSetController() throws NoSuchAlgorithmException {
        this.keyPair = generateKeyPair();
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    @GetMapping("/oauth2/jwks")
    public Map<String, Object> getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("kty", "RSA");
        jwks.put("alg", "RS256");
        jwks.put("use", "sig");
        jwks.put("kid", "exampleKeyId");
        jwks.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()));
        jwks.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray()));

        Map<String, Object> response = new HashMap<>();
        response.put("keys", List.of(jwks));
        System.out.println("sono in authService1/JwkSetController ");
        return response;
    }
}


