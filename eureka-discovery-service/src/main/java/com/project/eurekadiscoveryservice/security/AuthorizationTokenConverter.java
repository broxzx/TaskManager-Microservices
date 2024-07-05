package com.project.eurekadiscoveryservice.security;

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
public class AuthorizationTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
        Collection<String> roles = realmAccess.get("roles");

        List<SimpleGrantedAuthority> grantedAuthorities = roles.stream()
                .filter(potentialRole -> potentialRole.startsWith("ROLE_"))
                .map(SimpleGrantedAuthority::new)
                .toList();


        return new JwtAuthenticationToken(jwt, grantedAuthorities);
    }
}
