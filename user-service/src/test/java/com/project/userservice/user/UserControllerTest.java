package com.project.userservice.user;

import com.project.userservice.config.SecurityBeans;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.user.service.UserService;
import com.project.userservice.utils.KafkaProducerService;
import com.project.userservice.utils.KeycloakUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserController.class})
@Import(value = {SecurityBeans.class})
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

    private String baseUserJson;

    private String baseUserJsonWithBadValues;

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

        this.baseUserJson = """
                {
                    "username": "user",
                    "password": "1234",
                    "email": "user@mail.com",
                    "firstName": "John",
                    "lastName": "Doe",
                    "birthDate": "2024-09-24"
                }
                """;

        this.baseUserJsonWithBadValues = """
                {
                    "username": " ",
                    "password": " ",
                    "email": "user",
                    "firstName": " ",
                    "lastName": " ",
                    "birthDate": "2024-09-24"
                }
                """;
    }


    @Test
    void givenUserRequestWithValidFields_whenUserRegister_thenSuccess() throws Exception {
        User userFromUserRequest = buildUserFromUserRequest(baseUserRequest);
        when(userService.registerUser(baseUserRequest)).thenReturn(userFromUserRequest);

        this.mockMvc
                .perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baseUserJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.username").value(userFromUserRequest.getUsername()),
                        jsonPath("$.email").value(userFromUserRequest.getEmail()),
                        jsonPath("$.firstName").value(userFromUserRequest.getFirstName()),
                        jsonPath("$.lastName").value(userFromUserRequest.getLastName()),
                        jsonPath("$.birthDate").value("2024-09-24"));
    }

    @Test
    void givenUserRequestWithValidFields_whenUserRegisterWithAuthorizationHeader_thenRedirect() throws Exception {
        this.mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baseUserJson)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
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
    void givenUserRequestWithBadValues_whenUserRegister_thenFailure() throws Exception {
        this.mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(this.baseUserJsonWithBadValues))
                .andDo(print())
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.instance").value("/users/register")
                );
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
