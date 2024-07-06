package com.project.userservice.utils;

import com.project.userservice.user.data.UserEntity;
import com.project.userservice.user.data.service.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakUtils {

    private final RestTemplate restTemplate;
    private final UserService userService;

    @Value("${keycloak.token.request}")
    private String urlTokenRequest;

    @Value("${keycloak.token.client_id}")
    private String clientId;

    @Value("${keycloak.token.client_secret}")
    private String clientSecret;

    @Value("${keycloak.serverUrl}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;


    public String getUserTokenFromUsernameAndPassword(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> exchange = restTemplate.exchange(urlTokenRequest, HttpMethod.POST, request, Map.class);

        Map exchangeBody = exchange.getBody();
        return exchangeBody != null ? exchangeBody.get("access_token").toString() : null;
    }

    public void forgotPassword(String userId) {
        UserEntity obtainedUserEntity = userService.getUserEntityById(userId);

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();

        UserRepresentation user = keycloak.realm(realm)
                .users()
                .search(obtainedUserEntity.getUsername())
                .stream()
                .findFirst()
                .orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        keycloak.realm(realm)
                .users()
                .get(user.getId())
                .executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));

    }
}
