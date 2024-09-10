package com.project.taskservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class JwtTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Map<String, Collection<String>> realm_access = source.getClaim("realm_access");
        Collection<String> roles = realm_access.get("roles");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>(roles.stream()
                .filter(role -> role.startsWith("ROLE_"))
                .map(SimpleGrantedAuthority::new)
                .toList());

        List<String> scopes = source.getClaimAsStringList("scope");

        if (scopes != null) {
            authorities.addAll(Arrays.stream(scopes.get(0).split(" ")).map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)).toList());
        }

        return new JwtAuthenticationToken(source, authorities);
    }

}
