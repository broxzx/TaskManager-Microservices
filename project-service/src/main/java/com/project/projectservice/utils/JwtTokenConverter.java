package com.project.projectservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Map<String, Collection<String>> keycloakRoles = source.getClaim("realm_access");

        List<SimpleGrantedAuthority> userRoles = keycloakRoles.get("roles")
                .stream()
                .filter(role -> role.startsWith("ROLE_"))
                .map(SimpleGrantedAuthority::new)
                .toList();

        log.info("{}", userRoles);

        return new JwtAuthenticationToken(source, userRoles);
    }
}
