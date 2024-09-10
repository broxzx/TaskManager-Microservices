package com.project.taskservice.config;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.data.dto.ColumnRequest;
import com.project.taskservice.exceptions.TokenInvalidException;
import com.project.taskservice.utils.ErrorFeignDecoder;
import feign.RequestInterceptor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

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

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.typeMap(ColumnRequest.class, Column.class).addMappings(mapper -> mapper.skip(Column::setId));
        return modelMapper;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .realm(keycloakRealm)
                    .serverUrl(keycloakServerUrl)
                    .clientId(keycloakClientId)
                    .clientSecret(keycloakClientSecret)
                    .scope("view_users")
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();

            AccessTokenResponse accessToken = keycloak.tokenManager().getAccessToken();
            if (accessToken == null) {
                throw new TokenInvalidException("Invalid token");
            }

            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getToken());
        };
    }

    @Bean
    public ErrorFeignDecoder userErrorFeignDecoder() {
        return new ErrorFeignDecoder();
    }

}
