package com.project.userservice.utils;

import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.UserEntity;
import com.project.userservice.user.data.dto.response.UserResponse;
import com.project.userservice.user.data.service.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakUtils {

    private final RestTemplate restTemplate;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;

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


    public TokenResponse getUserTokenFromUsernameAndPassword(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);

        Map exchangeBody = tokenRequest(body, headers).getBody();
        return buildTokenResponseFromBodyExchange(Objects.requireNonNull(exchangeBody));
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

    public TokenResponse refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth2Constants.CLIENT_ID, clientId);
        body.add(OAuth2Constants.CLIENT_SECRET, clientSecret);
        body.add(OAuth2Constants.GRANT_TYPE, OAuth2Constants.REFRESH_TOKEN);
        body.add(OAuth2Constants.REFRESH_TOKEN, refreshToken);

        Map exchangeBody = tokenRequest(body, headers).getBody();
        return buildTokenResponseFromBodyExchange(Objects.requireNonNull(exchangeBody));
    }

    private TokenResponse buildTokenResponseFromBodyExchange(Map body) {
        String accessToken = body.get("access_token").toString();

        UserEntity obtainedUserEntity = userService.getUserEntityByUsername(jwtUtils.getUsernameByToken(accessToken));

        return new TokenResponse(body.get(OAuth2Constants.ACCESS_TOKEN).toString(),
                body.get(OAuth2Constants.REFRESH_TOKEN).toString(), Long.valueOf(body.get(OAuth2Constants.EXPIRES_IN).toString()),
                modelMapper.map(obtainedUserEntity, UserResponse.class));
    }

    private ResponseEntity<Map> tokenRequest(MultiValueMap<String, String> body, HttpHeaders headers) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.exchange(urlTokenRequest, HttpMethod.POST, request, Map.class);
    }
}
