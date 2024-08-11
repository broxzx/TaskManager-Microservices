package com.project.projectservice.config;

import feign.RequestInterceptor;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.http.HttpHeaders;

@Slf4j
@Configuration
public class BeanConfig {

    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .serverUrl(serverUrl)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .scope("view_users")
                    .build();

            AccessTokenResponse accessToken = keycloak.tokenManager().getAccessToken();

            log.info("{}", accessToken.getToken());

            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getToken());
        };
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(ObservationRegistry observationRegistry) {
        return builder -> builder.contextProvider(ContextProviderFactory.create(observationRegistry))
                .addCommandListener(new MongoObservationCommandListener(observationRegistry));
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return modelMapper;
    }

}
