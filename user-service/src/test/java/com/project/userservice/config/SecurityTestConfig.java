package com.project.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityTestConfig {

    // mocked jwt token
    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "user");
            claims.put("scope", "SCOPE_view_users");
            claims.put("realm_access", Map.of("roles", List.of("ROLE_ADMIN")));

            return new Jwt(
                    token,
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "none"),
                    claims
            );
        };
    }
}

