package com.project.userservice.user.service;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.exception.PasswordNotMatch;
import com.project.userservice.exception.UserAlreadyExistsException;
import com.project.userservice.model.ChangePasswordDto;
import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.UserEntity;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.data.enums.Roles;
import com.project.userservice.utils.JwtUtils;
import com.project.userservice.utils.KeycloakUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private KeycloakUtils keycloakUtils;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    public UserEntity getUserEntityById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(id)));
    }

    public UserEntity registerUser(UserRequest userRequest) {
        checkUserDuplicates(userRequest);

        UserEntity userEntityToSave = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .email(userRequest.getEmail())
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .birthDate(userRequest.getBirthDate())
                .roles(List.of(Roles.ROLE_USER.toString()))
                .build();

        return userRepository.save(userEntityToSave);
    }

    public UserEntity updateUserEntity(String authorizationToken, UserRequest userRequest) {
        String usernameByToken = jwtUtils.getUsernameByToken(jwtUtils.extractTokenFromHeader(authorizationToken));
        UserEntity userEntityToAlter = getUserEntityByUsername(usernameByToken);

        updateUserEntityFields(userEntityToAlter, userRequest);

        return userRepository.save(userEntityToAlter);
    }

    public UserEntity getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(username)));
    }

    private void checkUserDuplicates(UserRequest userRequest) {
        userRepository.findByUsername(userRequest.getUsername())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("user with username '%s' already exists".formatted(userRequest.getUsername()));
                });

        userRepository.findByEmail(userRequest.getEmail())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("user with email '%s' already exists".formatted(userRequest.getEmail()));
                });
    }

    private void updateUserEntityFields(UserEntity user, UserRequest userRequest) {
        if (userRequest.getUsername() != null) {
            user.setUsername(user.getUsername());
        }

        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
        }
    }

    public void changePasswordWithNewPassword(String token, ChangePasswordDto changePasswordDto) {
        String userEmail = jwtUtils.getEmailFromResetPasswordToken(token);

        UserEntity userToChangePassword = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("user with email %s is not found".formatted(userEmail)));

        if (!Objects.equals(changePasswordDto.password(), changePasswordDto.confirmPassword())) {
            throw new PasswordNotMatch("passwords don't match");
        }

        userToChangePassword.setPassword(bCryptPasswordEncoder.encode(changePasswordDto.password()));
        userRepository.save(userToChangePassword);
    }

    public void processGrantCode(String grantCode, HttpServletResponse response) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", grantCode);
        params.add("redirect_uri", "http://localhost:8080/users/grantCode");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.addAll("scope", List.of("https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email", "openid"));
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

        String url = "https://oauth2.googleapis.com/token";

        Map obtainedDataFromCode = restTemplate.postForObject(url, requestEntity, Map.class);

        JsonObject userProfileDetails = getProfileDetailsGoogle(Objects.requireNonNull(obtainedDataFromCode).get("access_token").toString());

        UserEntity userEntityToSave = UserEntity.builder()
                .username(userProfileDetails.get("email").toString().replace("\"", ""))
                .password(bCryptPasswordEncoder.encode(userProfileDetails.get("id").toString().replace("\"", "")))
                .email(userProfileDetails.get("email").toString().replace("\"", ""))
                .emailVerified(Boolean.valueOf(userProfileDetails.get("verified_email").toString().replace("\"", "")))
                .googleAccountId(userProfileDetails.get("id").toString().replace("\"", ""))
                .firstName(userProfileDetails.get("given_name").toString().replace("\"", ""))
                .lastName(userProfileDetails.get("given_name").toString().replace("\"", ""))
                .profilePictureUrl(userProfileDetails.get("picture").toString().replace("\"", ""))
                .birthDate(LocalDate.now())
                .build();


        userRepository.save(userEntityToSave);

        log.info("{}", userProfileDetails);

        TokenResponse userTokenResponse = keycloakUtils.getUserTokenFromUsernameAndPassword(userEntityToSave.getUsername(), userProfileDetails.get("id").toString().replace("\"", ""), true);

        try {
            String encodedValue = URLEncoder.encode(userTokenResponse.accessToken(), StandardCharsets.UTF_8);

            Cookie cookie = new Cookie(HttpHeaders.AUTHORIZATION, encodedValue);
            response.addCookie(cookie);

            response.sendRedirect("http://localhost:8080/users/dashboard");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonObject getProfileDetailsGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        return new Gson().fromJson(response.getBody(), JsonObject.class);
    }

    public String getUserIdByToken(String token) {
        String username = jwtUtils.getUsernameByToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("user with username '%s' is not found".formatted(username)))
                .getId();
    }

    @Autowired
    public void setKeycloakUtils(@Lazy KeycloakUtils keycloakUtils) {
        this.keycloakUtils = keycloakUtils;
    }
}
