package com.project.projectservice.utils;

import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    public String extractTokenFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token is not valid");
        }

        return authorizationHeader.substring(7);
    }

}
