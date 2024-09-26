package com.project.userservice.user;

import com.project.userservice.config.SecurityBeans;
import com.project.userservice.config.SecurityTestConfig;
import com.project.userservice.model.TokenResponse;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.LoginRequest;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.data.dto.response.UserResponse;
import com.project.userservice.user.service.UserService;
import com.project.userservice.utils.KafkaProducerService;
import com.project.userservice.utils.KeycloakUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserController.class})
@Import(value = {SecurityBeans.class, SecurityTestConfig.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private KeycloakUtils keycloakUtils;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    private UserRequest baseUserRequest;

    private String validBaseUserJson;

    private String invalidBaseUserJson;

    private String token;

    @BeforeEach
    public void setUp() {
        this.baseUserRequest = UserRequest.builder()
                .username("user")
                .password("1234")
                .email("user@mail.com")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(2024, 9, 24))
                .build();

        this.validBaseUserJson = """
                {
                    "username": "user",
                    "password": "1234",
                    "email": "user@mail.com",
                    "firstName": "John",
                    "lastName": "Doe",
                    "birthDate": "2024-09-24"
                }
                """;

        this.invalidBaseUserJson = """
                {
                    "username": " ",
                    "password": " ",
                    "email": "user",
                    "firstName": " ",
                    "lastName": " ",
                    "birthDate": "2024-09-24"
                }
                """;

        this.token = "mocked-jwt-token";
    }


    @Test
    void givenUserRequestWithValidFields_whenUserRegister_thenSuccess() throws Exception {
        User userFromUserRequest = buildUserFromUserRequest(baseUserRequest);
        when(userService.registerUser(baseUserRequest)).thenReturn(userFromUserRequest);

        this.mockMvc
                .perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBaseUserJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.username").value(userFromUserRequest.getUsername()),
                        jsonPath("$.email").value(userFromUserRequest.getEmail()),
                        jsonPath("$.firstName").value(userFromUserRequest.getFirstName()),
                        jsonPath("$.lastName").value(userFromUserRequest.getLastName()),
                        jsonPath("$.birthDate").value("2024-09-24"));
    }

    @Test
    void givenUserRequestWithValidFields_whenLoggedUserRegister_thenRedirect() throws Exception {
        this.mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBaseUserJson)
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.username").doesNotExist(),
                        jsonPath("$.email").doesNotExist(),
                        jsonPath("$.firstName").doesNotExist(),
                        jsonPath("$.lastName").doesNotExist(),
                        jsonPath("$.birthDate").doesNotExist()
                );
    }

    @Test
    void givenUserRequestIsNull_whenUserRegister_thenFailure() throws Exception {
        this.mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.title").value("Unprocessable Entity"),
                        jsonPath("$.status").value(422),
                        jsonPath("$.instance").value("/users/register")
                );
    }

    @Test
    void givenUserRequestWithInvalidValues_whenUserRegister_thenFailure() throws Exception {
        this.mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(this.invalidBaseUserJson))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.instance").value("/users/register")
                );
    }

    @Test
    void givenValidLoginRequest_whenUserLogin_thenReturnTokenResponse() throws Exception {
        LoginRequest userRequest = new LoginRequest("user", "1234", false);
        String loginRequest = """
                {
                    "username": "user",
                    "password": "1234",
                    "rememberMe": false
                }
                """;

        long expiresIn = new Date().getTime() + 3600;
        TokenResponse tokenResponse = new TokenResponse("token", "refresh_token",
                expiresIn, new UserResponse("user", "user@mail.com", List.of("ROLE_USER")));

        when(keycloakUtils.getUserTokenFromUsernameAndPassword(userRequest.getUsername(), userRequest.getPassword(),
                userRequest.isRememberMe())).thenReturn(tokenResponse);

        this.mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.accessToken").value("token"),
                        jsonPath("$.refreshToken").value("refresh_token"),
                        jsonPath("$.expiresIn").value(expiresIn),
                        jsonPath("$.userResponse.username").value("user"),
                        jsonPath("$.userResponse.email").value("user@mail.com"),
                        jsonPath("$.userResponse.roles[0]").value("ROLE_USER")
                );
    }

    @Test
    void givenValidLoginRequest_whenLoggedUserLogin_thenRedirect() throws Exception {
        String loginRequest = """
                {
                    "username": "user",
                    "password": "1234",
                    "rememberMe": false
                }
                """;

        this.mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest)
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").doesNotExist(),
                        jsonPath("$.refreshToken").doesNotExist(),
                        jsonPath("$.expiresIn").doesNotExist(),
                        jsonPath("$.userResponse.username").doesNotExist(),
                        jsonPath("$.userResponse.email").doesNotExist(),
                        jsonPath("$.userResponse.roles[0]").doesNotExist()
                );
    }

    @Test
    void givenInvalidLoginRequest_whenUserLogin_thenFailure() throws Exception {
        String invalidLoginRequest = """
                {
                    "username": " ",
                    "password": " ",
                    "rememberMe": false
                }
                """;

        this.mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginRequest))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value("/users/login"),
                        jsonPath("$.occurred").exists()
                );
    }

    @Test
    void givenValidUserRequest_whenUserUpdateData_thenSuccess() throws Exception {
        when(userService.updateUserEntity(token, this.baseUserRequest)).thenReturn(buildUserFromUserRequest(baseUserRequest));

        this.mockMvc.perform(put("/users/updateUserData")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.validBaseUserJson)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .with(buildPostProcessorWithUserRole())
                )
                .andDo(print())
                .andExpectAll(
                        status().isOk()
                );
    }

    @Test
    void givenValidUserRequest_whenAnonymousUserUpdateData_thenFailure() throws Exception {
        this.mockMvc.perform(put("/users/updateUserData")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBaseUserJson)
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenInvalidUserRequest_whenUserLogin_thenFailure() throws Exception {
        System.out.println(HttpStatus.BAD_REQUEST.getReasonPhrase());

        this.mockMvc.perform(put("/users/updateUserData")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBaseUserJson)
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value("/users/updateUserData"),
                        jsonPath("$.occurred").exists(),
                        jsonPath("$.errors").isArray()
                );
    }

    @Test
    void givenValidUserId_whenUserForgotPassword_thenSuccess() throws Exception {
        this.mockMvc.perform(post("/users/forgotPassword")
                        .param("userId", UUID.randomUUID().toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(token))
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void givenAbsentUserId_whenUserForgotPassword_thenFailure() throws Exception {
        this.mockMvc.perform(post("/users/forgotPassword")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(token))
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value("/users/forgotPassword"),
                        jsonPath("$.occurred").exists()
                );
    }

    @Test
    void givenValidUserEmail_whenUserResetPassword_thenSuccess() throws Exception {
        this.mockMvc.perform(post("/users/resetPassword")
                        .param("email", this.baseUserRequest.getEmail())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(token))
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void givenAbsentUserEmail_whenUserResetPassword_thenSuccess() throws Exception {
        this.mockMvc.perform(post("/users/resetPassword")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(token))
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value("/users/resetPassword"),
                        jsonPath("$.occurred").exists()
                );
    }

    @Test
    void givenValidUserEmail_whenAnonymousUserResetPassword_thenSuccess() throws Exception {
        this.mockMvc.perform(post("/users/resetPassword")
                        .param("email", this.baseUserRequest.getEmail())
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor buildPostProcessorWithUserRole() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private RequestPostProcessor buildPostProcessorWithAnonymousUser() {
        return SecurityMockMvcRequestPostProcessors.anonymous();
    }

    private User buildUserFromUserRequest(UserRequest basicUserRequest) {
        return User.builder()
                .username(basicUserRequest.getUsername())
                .password(basicUserRequest.getPassword())
                .email(basicUserRequest.getEmail())
                .firstName(basicUserRequest.getFirstName())
                .lastName(basicUserRequest.getLastName())
                .birthDate(basicUserRequest.getBirthDate())
                .build();
    }


}
