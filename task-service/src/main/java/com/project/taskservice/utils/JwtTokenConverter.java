package com.project.taskservice.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        List<String> groups = source.getClaimAsStringList("groups");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>(groups.stream()
                .filter(group -> group.startsWith("ROLE_"))
                .map(SimpleGrantedAuthority::new)
                .toList());

        List<String> scopes = source.getClaimAsStringList("scope");

        if (scopes != null && !scopes.isEmpty()) {
            authorities.addAll(Arrays.stream(scopes.get(0).split(" ")).map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)).toList());
        }

        return new JwtAuthenticationToken(source, authorities);
    }

}
