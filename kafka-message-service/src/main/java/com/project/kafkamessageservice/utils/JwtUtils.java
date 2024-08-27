package com.project.kafkamessageservice.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expires}")
    private Duration expirationTime;

    /**
     * generates token for changing password
     *
     * @param email represents user's email
     * @return user's changing password token
     */
    public String generateResetPasswordToken(String email) {
        Map<String, String> claims = Map.of("resetPasswordToken", "true");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime.toMillis()))
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
