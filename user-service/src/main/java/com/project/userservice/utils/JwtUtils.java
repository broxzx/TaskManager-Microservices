package com.project.userservice.utils;

import com.project.userservice.exception.ResetPasswordTokenIncorrectException;
import com.project.userservice.exception.TokenInvalidException;
import lombok.SneakyThrows;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    public String getUsernameByToken(String token) {
        JwtClaims claims = getJwtClaims(token);

        return (String) claims.getClaimValue("preferred_username");
    }

    public String getUserIdByToken(String token) {
        JwtClaims claims = getJwtClaims(token);

        return claims.getClaimValueAsString("user_id");
    }

    @SneakyThrows
    public String getEmailFromResetPasswordToken(String token) {
        JwtClaims claims = getJwtClaims(token);

        if (claims.getClaimValue("resetPasswordToken") == null || !claims.getClaimValue("resetPasswordToken").equals("true")) {
            throw new ResetPasswordTokenIncorrectException("reset password token is invalid");
        }

        return claims
                .getSubject();
    }

    private static JwtClaims getJwtClaims(String token) {
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
        return claims;
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new TokenInvalidException("Invalid JWT token");
        }

        return authorizationHeader.substring(7);
    }

}

