package com.project.userservice.config;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.netflix.eureka.RestTemplateTimeoutProperties;
import org.springframework.cloud.netflix.eureka.http.DefaultEurekaClientHttpRequestFactorySupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.List;
import java.util.Objects;

@Configuration
public class DiscoveryBeans {

    @Bean
    public DefaultEurekaClientHttpRequestFactorySupplier defaultEurekaClientHttpRequestFactorySupplier(
            RestTemplateTimeoutProperties restTemplateTimeoutProperties,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService auth2AuthorizedClientService
    ) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, auth2AuthorizedClientService);

        return new DefaultEurekaClientHttpRequestFactorySupplier(restTemplateTimeoutProperties,
                List.of(((httpRequest, entityDetails, httpContext) -> {
                    if (!httpRequest.containsHeader(HttpHeaders.AUTHORIZATION)) {
                        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                                .withClientRegistrationId("keycloak")
                                .principal("user-service")
                                .build();

                        OAuth2AuthorizedClient keycloak = authorizedClientManager.authorize(authorizeRequest);

                        if (keycloak != null && keycloak.getAccessToken() != null) {
                            httpRequest.setHeader(HttpHeaders.AUTHORIZATION,
                                    "Bearer %s".formatted(keycloak.getAccessToken().getTokenValue()));
                        } else {
                            throw new RuntimeException("Authorization failed for client keycloak");
                        }
                    }
                })));
    }
}
