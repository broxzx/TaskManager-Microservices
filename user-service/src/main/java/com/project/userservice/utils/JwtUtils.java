package com.project.userservice.utils;

import com.project.userservice.exception.DefaultException;
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

    /**
     * getting username claim from token
     *
     * @param token represents user's token
     * @return claim value "preferred_username" from token
     */
    public String getUsernameByToken(String token) {
        JwtClaims claims = getJwtClaims(token);

        return (String) claims.getClaimValue("preferred_username");
    }

    /**
     * getting user's id from token
     *
     * @param token represents user's token
     * @return claim value "user_id" from token
     */
    public String getUserIdByToken(String token) {
        JwtClaims claims = getJwtClaims(token);
        String userId = claims.getClaimValueAsString("user_id");

        if (userId == null || userId.isBlank()) {
            throw new DefaultException("claim \"user_id\" is not found in token");
        }

        return userId;
    }

    /**
     * used when user called for changing password
     *
     * @param token generated token for changing credentials
     * @return user's email
     */
    @SneakyThrows
    public String getEmailFromResetPasswordToken(String token) {
        JwtClaims claims = getJwtClaims(token);

        if (claims.getClaimValue("resetPasswordToken") == null || !claims.getClaimValue("resetPasswordToken").equals("true")) {
            throw new ResetPasswordTokenIncorrectException("reset password token is invalid");
        }

        return claims
                .getSubject();
    }

    /**
     * method for obtaining claims from keycloak jwt token
     *
     * @param token
     * @return
     */
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

    /**
     * method for obtaining token from Authorization header
     *
     * @param authorizationHeader represents user's authorization header
     * @return jwt token
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new TokenInvalidException("Invalid JWT token");
        }

        return authorizationHeader.substring(7);
    }

}

