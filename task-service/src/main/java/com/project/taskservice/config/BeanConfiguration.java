package com.project.taskservice.config;

import com.project.taskservice.exceptions.TokenInvalidException;
import feign.RequestInterceptor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Configuration
public class BeanConfiguration {

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    @Value("${keycloak.realm}")
    private String keycloakRealm;
    @Value("${keycloak.client-id}")
    private String keycloakClientId;
    @Value("${keycloak.client-secret}")
    private String keycloakClientSecret;
    @Value("${keycloak.scope}")
    private String keycloakScope;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return modelMapper;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(keycloakRealm)
                    .clientId(keycloakClientId)
                    .clientSecret(keycloakClientSecret)
                    .scope(keycloakScope)
                    .build();

            AccessTokenResponse accessToken = keycloak.tokenManager().getAccessToken();
            if (accessToken == null) {
                throw new TokenInvalidException("Invalid token");
            }

            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getToken());
        };
    }

}
