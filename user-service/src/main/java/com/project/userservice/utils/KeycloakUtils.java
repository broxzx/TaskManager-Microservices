package com.project.userservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakUtils {

    private final RestTemplate restTemplate;

    @Value("${keycloak.token.request}")
    private String urlTokenRequest;

    @Value("${keycloak.token.client_id}")
    private String clientId;

    @Value("${keycloak.token.client_secret}")
    private String clientSecret;


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

}
