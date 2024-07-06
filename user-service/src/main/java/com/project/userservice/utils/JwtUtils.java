package com.project.userservice.utils;

import com.project.userservice.exception.TokenInvalid;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    public String getUsernameByToken(String token) {
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setSkipAllValidators()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();
        JwtClaims claims;
        try {
            claims = consumer.processToClaims(token);
        } catch (InvalidJwtException e) {
            throw new RuntimeException(e);
        }

        return (String) claims.getClaimValue("preferred_username");
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new TokenInvalid("Invalid JWT token");
        }

        return authorizationHeader.substring(7);
    }

}

