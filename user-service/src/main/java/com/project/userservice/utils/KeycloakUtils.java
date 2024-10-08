package com.project.userservice.utils;

import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.response.UserResponse;
import com.project.userservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static org.keycloak.OAuth2Constants.*;

@Slf4j
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

    /**
     * method for obtaining access and refresh token using user's credentials (keycloak jwt)
     *
     * @param username   represents user's username
     * @param password   represents user's password
     * @param rememberMe represents user's choice whether to prolong lifetime of token or not
     * @return access and refresh token from keycloak authorization server
     */
    public TokenResponse getUserTokenFromUsernameAndPassword(String username, String password, boolean rememberMe) {
        HttpHeaders headers = buildHttpHeadersToObtainToken();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, PASSWORD);
        body.add(CLIENT_ID, clientId);
        body.add(CLIENT_SECRET, clientSecret);
        body.add(USERNAME, username);
        body.add(PASSWORD, password);
        body.add(SCOPE, "id-mapper");

        if (rememberMe) {
            body.set(SCOPE, "id-mapper " + OFFLINE_ACCESS);
        }

        Map exchangeBody = tokenRequest(body, headers).getBody();
        log.info("{}", buildTokenResponseFromBodyExchange(Objects.requireNonNull(exchangeBody)));
        return buildTokenResponseFromBodyExchange(Objects.requireNonNull(exchangeBody));
    }

    /**
     * force user to change password next time when he authorizes
     *
     * @param userId represents user's id
     */
    public void forgotPassword(String userId) {
        User obtainedUser = userService.getUserById(userId);

        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(CLIENT_CREDENTIALS)
                .build()) {

            UserRepresentation user = keycloak.realm(realm)
                    .users()
                    .search(obtainedUser.getUsername())
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

    /**
     * used for refreshing token
     *
     * @param refreshToken represents refresh token obtained from keycloak auth. server
     * @return new access and refresh tokens
     */
    public TokenResponse refreshToken(String refreshToken) {
        HttpHeaders headers = buildHttpHeadersToObtainToken();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(CLIENT_ID, clientId);
        body.add(CLIENT_SECRET, clientSecret);
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, refreshToken);

        Map exchangeBody = tokenRequest(body, headers).getBody();
        return buildTokenResponseFromBodyExchange(Objects.requireNonNull(exchangeBody));
    }

    /**
     * used to send http request for obtaining token
     *
     * @return headers with appropriate content type for keycloak
     */
    private static HttpHeaders buildHttpHeadersToObtainToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    /**
     * builds TokenResponse model using obtained Map
     *
     * @param body represents response from keycloak
     * @return access and response tokens
     */
    private TokenResponse buildTokenResponseFromBodyExchange(Map body) {
        String accessToken = body.get(ACCESS_TOKEN).toString();

        User obtainedUser = userService.getUserById(jwtUtils.getUserIdByToken(accessToken));

        return new TokenResponse(body.get(ACCESS_TOKEN).toString(),
                body.get(REFRESH_TOKEN).toString(), Long.valueOf(body.get(EXPIRES_IN).toString()),
                modelMapper.map(obtainedUser, UserResponse.class));
    }

    /**
     * builds request to obtain token
     *
     * @param body    represents body key/values
     * @param headers represents built headers for request
     * @return response from keycloak
     */
    private ResponseEntity<Map> tokenRequest(MultiValueMap<String, String> body, HttpHeaders headers) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.exchange(urlTokenRequest, HttpMethod.POST, request, Map.class);
    }
}
