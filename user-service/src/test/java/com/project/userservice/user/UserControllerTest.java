package com.project.userservice.user;

import com.project.userservice.config.SecurityBeans;
import com.project.userservice.config.SecurityTestConfig;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.LoginRequest;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.service.UserService;
import com.project.userservice.utils.KafkaProducerService;
import com.project.userservice.utils.KeycloakUtils;
import com.project.userservice.testUtils.SecurityUtils;
import com.project.userservice.testUtils.UserUtils;
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

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void givenUserRequestWithValidFields_whenUserRegister_thenSuccess() throws Exception {
        final String url = "/users/register";

        User userFromUserRequest = buildUserFromUserRequest(UserUtils.baseUserRequest());
        when(userService.registerUser(UserUtils.baseUserRequest())).thenReturn(userFromUserRequest);

        this.mockMvc
                .perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(UserUtils.validBaseUserJson()))
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
        final String url = "/users/register";
        final String expectedRedirectUrl = "http://localhost:8081/projects/getUserProjects";

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(UserUtils.validBaseUserJson())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().isFound(),
                        header().string(HttpHeaders.LOCATION, expectedRedirectUrl),
                        jsonPath("$.username").doesNotExist(),
                        jsonPath("$.email").doesNotExist(),
                        jsonPath("$.firstName").doesNotExist(),
                        jsonPath("$.lastName").doesNotExist(),
                        jsonPath("$.birthDate").doesNotExist()
                );
    }

    @Test
    void givenUserRequestIsNull_whenUserRegister_thenFailure() throws Exception {
        final String url = "/users/register";

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.title").value(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()),
                        jsonPath("$.instance").value(url)
                );
    }

    @Test
    void givenUserRequestWithInvalidValues_whenUserRegister_thenFailure() throws Exception {
        final String url = "/users/register";

        this.mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(UserUtils.invalidBaseUserJson()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.instance").value(url)
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
        final String url = "/users/login";

        when(keycloakUtils.getUserTokenFromUsernameAndPassword(userRequest.getUsername(), userRequest.getPassword(),
                userRequest.isRememberMe())).thenReturn(SecurityUtils.tokenResponse());

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.accessToken").value(SecurityUtils.tokenResponse().accessToken()),
                        jsonPath("$.refreshToken").value(SecurityUtils.tokenResponse().refreshToken()),
                        jsonPath("$.expiresIn").exists(),
                        jsonPath("$.userResponse.username").value(SecurityUtils.tokenResponse().userResponse().getUsername()),
                        jsonPath("$.userResponse.email").value(SecurityUtils.tokenResponse().userResponse().getEmail()),
                        jsonPath("$.userResponse.roles[0]").value(SecurityUtils.tokenResponse().userResponse().getRoles().get(0))
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
        final String url = "/users/login";
        final String expectedRedirectUrl = "http://localhost:8081/projects/getUserProjects";

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest)
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().isFound(),
                        header().string(HttpHeaders.LOCATION, expectedRedirectUrl),
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
        final String url = "/users/login";

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(invalidLoginRequest))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value(url),
                        jsonPath("$.occurred").exists()
                );
    }

    @Test
    void givenValidUserRequest_whenUserUpdateData_thenSuccess() throws Exception {
        final String url = "/users/updateUserData";
        final String token = "mocked-jwt-token";

        when(userService.updateUserEntity(token, UserUtils.baseUserRequest())).thenReturn(buildUserFromUserRequest(UserUtils.baseUserRequest()));

        this.mockMvc.perform(put(url)
                        .contentType(APPLICATION_JSON)
                        .content(UserUtils.validBaseUserJson())
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithUserRole())
                )
                .andDo(print())
                .andExpectAll(
                        status().isOk()
                );
    }

    @Test
    void givenValidUserRequest_whenAnonymousUserUpdateData_thenFailure() throws Exception {
        final String url = "/users/updateUserData";

        this.mockMvc.perform(put(url)
                        .contentType(APPLICATION_JSON)
                        .content(UserUtils.validBaseUserJson())
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenInvalidUserRequest_whenUserUpdateData_thenFailure() throws Exception {
        final String url = "/users/updateUserData";

        this.mockMvc.perform(put(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .contentType(APPLICATION_JSON)
                        .content(UserUtils.invalidBaseUserJson())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value(url),
                        jsonPath("$.occurred").exists(),
                        jsonPath("$.errors").isArray()
                );
    }

    @Test
    void givenValidUserId_whenUserForgotPassword_thenSuccess() throws Exception {
        final String url = "/users/forgotPassword";
        String randomUUID = UUID.randomUUID().toString();

        this.mockMvc.perform(post(url)
                        .param("userId", randomUUID)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void givenAbsentUserId_whenUserForgotPassword_thenFailure() throws Exception {
        final String url = "/users/forgotPassword";

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value(url),
                        jsonPath("$.occurred").exists()
                );
    }

    @Test
    void givenValidUserEmail_whenUserResetPassword_thenSuccess() throws Exception {
        final String url = "/users/resetPassword";

        this.mockMvc.perform(post(url)
                        .param("email", UserUtils.baseUserRequest().getEmail())
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void givenAbsentUserEmail_whenUserResetPassword_thenSuccess() throws Exception {
        final String url = "/users/resetPassword";

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.detail").exists(),
                        jsonPath("$.instance").value(url),
                        jsonPath("$.occurred").exists()
                );
    }

    @Test
    void givenValidUserEmail_whenAnonymousUserResetPassword_thenSuccess() throws Exception {
        final String url = "/users/resetPassword";

        this.mockMvc.perform(post(url)
                        .param("email", UserUtils.baseUserRequest().getEmail())
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void givenTokenAndChangePasswordDto_whenAnonymousUserChangePassword_thenSuccess() throws Exception {
        final String url = "/users/changePassword";
        String changePasswordDtoJson = """
                {
                    "password": "1234",
                    "confirmPassword": "1234"
                }
                """;

        this.mockMvc.perform(post(url)
                        .param("token", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content(changePasswordDtoJson)
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void givenTokenAndChangePasswordDto_whenLoggedUserChangePassword_thenSuccess() throws Exception {
        final String url = "/users/changePassword";
        String changePasswordDtoJson = """
                {
                    "password": "1234",
                    "confirmPassword": "1234"
                }
                """;

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .param("token", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content(changePasswordDtoJson)
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void givenRefreshToken_whenAnonymousRefreshToken_thenSuccess() throws Exception {
        final String url = "/users/refreshToken";
        final String refreshToken = UUID.randomUUID().toString();

        when(keycloakUtils.refreshToken(refreshToken)).thenReturn(SecurityUtils.tokenResponse());

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(refreshToken)
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.accessToken").value(SecurityUtils.tokenResponse().accessToken()),
                        jsonPath("$.refreshToken").value(SecurityUtils.tokenResponse().refreshToken()),
                        jsonPath("$.expiresIn").exists(),
                        jsonPath("$.userResponse.username").value(SecurityUtils.tokenResponse().userResponse().getUsername()),
                        jsonPath("$.userResponse.email").value(SecurityUtils.tokenResponse().userResponse().getEmail()),
                        jsonPath("$.userResponse.roles[0]").value(SecurityUtils.tokenResponse().userResponse().getRoles().get(0))
                );
    }


    @Test
    void givenValidToken_whenClientGetUserIdByToken_thenSuccess() throws Exception {
        final String token = UUID.randomUUID().toString();
        final String url = "/users/getUserIdByToken";

        when(userService.getUserIdByToken(token)).thenReturn(UUID.randomUUID().toString());

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(token)
                        .with(buildPostProcessorWithScopeViewUsers()))
                .andDo(print())
                .andExpectAll(
                        status().isOk()
                );
    }

    @Test
    void givenValidToken_whenLoggedUserGetUserId_thenFailure() throws Exception {
        final String token = UUID.randomUUID().toString();
        final String url = "/users/getUserIdByToken";

        this.mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON)
                        .content(token)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.error").exists()
                );
    }

    @Test
    void givenValidToken_whenAnonymousUserGetUserIdByToken_thenFailure() throws Exception {
        final String token = UUID.randomUUID().toString();
        final String url = "/users/getUserIdByToken";

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .contentType(APPLICATION_JSON)
                        .content(token)
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.error").exists()
                );

    }

    @Test
    void givenValidUserId_whenClientCheckWhetherUserExists_thenFailure() throws Exception {
        final String url = "/users/checkUserExists";
        final String userId = UUID.randomUUID().toString();

        when(userService.checkUserExists(userId)).thenReturn(true);

        this.mockMvc.perform(get(url)
                        .param("userId", userId)
                        .with(buildPostProcessorWithScopeViewUsers()))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(APPLICATION_JSON),
                        content().string("true")
                );
    }

    @Test
    void givenValidUserId_whenLoggedUserCheckWhetherUserExists_thenFailure() throws Exception {
        final String url = "/users/checkUserExists";
        final String userId = UUID.randomUUID().toString();

        this.mockMvc.perform(get(url)
                        .param("userId", userId)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithUserRole()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.error").exists()
                );
    }

    @Test
    void givenValidUserId_whenAnonymousUserCheckWhetherUserExists_thenFailure() throws Exception {
        final String url = "/users/checkUserExists";
        final String userId = UUID.randomUUID().toString();

        this.mockMvc.perform(get(url)
                        .param("userId", userId)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.authorizationHeader())
                        .with(buildPostProcessorWithAnonymousUser()))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.error").exists()
                );
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor buildPostProcessorWithUserRole() {
        return SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private RequestPostProcessor buildPostProcessorWithAnonymousUser() {
        return SecurityMockMvcRequestPostProcessors
                .anonymous();
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor buildPostProcessorWithScopeViewUsers() {
        return SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority("SCOPE_view_users"));
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
