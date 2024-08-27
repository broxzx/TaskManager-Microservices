package com.project.userservice.user.service;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.exception.PasswordNotMatch;
import com.project.userservice.exception.UserAlreadyExistsException;
import com.project.userservice.model.ChangePasswordDto;
import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.data.enums.Roles;
import com.project.userservice.utils.JwtUtils;
import com.project.userservice.utils.KeycloakUtils;
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

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    /**
     * called by controller class to register user
     *
     * @param userRequest represents user that will be registered
     * @return User's model representing that user was successfully created
     */
    public User registerUser(UserRequest userRequest) {
        checkUserDuplicates(userRequest);

        User userToSave = User.builder()
                .username(userRequest.getUsername())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .email(userRequest.getEmail())
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .birthDate(userRequest.getBirthDate())
                .roles(List.of(Roles.ROLE_USER.toString()))
                .build();

        return userRepository.save(userToSave);
    }

    /**
     * usually called by controller to update user's data
     *
     * @param authorizationToken represents authorization header
     * @param userRequest        represents user's data to be updated
     * @return Updated user's model
     */
    public User updateUserEntity(String authorizationToken, UserRequest userRequest) {
        String userIdByToken = jwtUtils.getUserIdByToken(jwtUtils.extractTokenFromHeader(authorizationToken));
        User userToAlter = getUserById(userIdByToken);

        updateUserEntityFields(userToAlter, userRequest);

        return userRepository.save(userToAlter);
    }

    /**
     * used for obtaining user by id
     *
     * @param id represents user's id
     * @return User's model
     */
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(id)));
    }

    /**
     * used when updates password from email (using self-generated jwt token)
     *
     * @param token             represents token that user has in his email
     * @param changePasswordDto new password credentials
     */
    public void changePasswordWithNewPassword(String token, ChangePasswordDto changePasswordDto) {
        String userEmail = jwtUtils.getEmailFromResetPasswordToken(token);

        User userToChangePassword = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("user with email %s is not found".formatted(userEmail)));

        if (!Objects.equals(changePasswordDto.password(), changePasswordDto.confirmPassword())) {
            throw new PasswordNotMatch("passwords don't match");
        }

        userToChangePassword.setPassword(bCryptPasswordEncoder.encode(changePasswordDto.password()));
        userRepository.save(userToChangePassword);
    }

    /**
     * used when authorize user using google OAuth 2.0
     *
     * @param grantCode represents code that was given by google authorization server
     * @return TokenResponse to permit user use protected endpoints
     */
    public TokenResponse processGrantCode(String grantCode) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", grantCode);
        params.add("redirect_uri", googleRedirectUri);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.addAll("scope", List.of("https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email", "openid"));
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

        String url = "https://oauth2.googleapis.com/token";

        Map obtainedDataFromCode = restTemplate.postForObject(url, requestEntity, Map.class);

        JsonObject userProfileDetails = getProfileDetailsGoogle(Objects.requireNonNull(obtainedDataFromCode).get("access_token").toString());

        String googleAccountId = userProfileDetails.get("id").toString().replace("\"", "");
        User userToSave = User.builder()
                .username(userProfileDetails.get("email").toString().replace("\"", ""))
                .password(bCryptPasswordEncoder.encode(googleAccountId))
                .email(userProfileDetails.get("email").toString().replace("\"", ""))
                .emailVerified(Boolean.parseBoolean(userProfileDetails.get("verified_email").toString().replace("\"", "")))
                .googleAccountId(googleAccountId)
                .firstName(userProfileDetails.get("given_name").toString().replace("\"", ""))
                .lastName(userProfileDetails.get("given_name").toString().replace("\"", ""))
                .profilePictureUrl(userProfileDetails.get("picture").toString().replace("\"", ""))
                .birthDate(LocalDate.now())
                .build();


        userRepository.save(userToSave);
        log.info("{}", userProfileDetails);

        return keycloakUtils.getUserTokenFromUsernameAndPassword(userToSave.getUsername(), googleAccountId, true);
    }

    /**
     * retrieves user's id from token
     *
     * @param token represents user's token
     * @return String user's id
     */
    public String getUserIdByToken(String token) {
        return jwtUtils.getUserIdByToken(token);
    }

    /**
     * used to avoid dependency circle
     *
     * @param keycloakUtils that will be injected
     */
    @Autowired
    public void setKeycloakUtils(@Lazy KeycloakUtils keycloakUtils) {
        this.keycloakUtils = keycloakUtils;
    }

    /**
     * used to check user existence in db
     *
     * @param userId represents user's id
     * @return whether user exists
     */
    public boolean checkUserExists(String userId) {
        return userRepository.existsById(userId);
    }

    /**
     * used when there should be check that user doesn't have multiple records in db
     *
     * @param userRequest represents user's data
     */
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

    /**
     * called when user's data should be updated using UserRequest
     *
     * @param user        represents user that will be updated
     * @param userRequest represents updated data
     */
    private void updateUserEntityFields(User user, UserRequest userRequest) {
        if (userRequest.getUsername() != null && !userRequest.getUsername().isBlank()) {
            user.setUsername(user.getUsername());
        }

        if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
            user.setEmail(userRequest.getEmail());
        }

        if (userRequest.getFirstName() != null && !userRequest.getFirstName().isBlank()) {
            user.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null && !userRequest.getLastName().isBlank()) {
            user.setLastName(userRequest.getLastName());
        }

    }

    /**
     * retrieves user's data in google account
     *
     * @param accessToken represents token that was given by google authorization server
     * @return user's info from Google
     */
    private JsonObject getProfileDetailsGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        return new Gson().fromJson(response.getBody(), JsonObject.class);
    }
}
