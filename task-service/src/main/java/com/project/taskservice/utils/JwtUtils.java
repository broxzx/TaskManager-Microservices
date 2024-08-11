package com.project.taskservice.utils;

import com.project.taskservice.exceptions.TokenInvalidException;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    public String getTokenFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new TokenInvalidException("Token invalid");
        }

        return authorizationHeader.substring(7);
    }

}
