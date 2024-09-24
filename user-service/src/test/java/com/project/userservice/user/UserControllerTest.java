package com.project.userservice.user;

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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class})
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

    @BeforeEach
    public void setUp() {
        baseUserRequest = UserRequest.builder()
                .username("user")
                .password("1234")
                .email("user@mail.com")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(2024, 9, 24))
                .build();
    }


    @Test
    void givenUserRequestWithValidFields_whenUserRegister_thenSuccess() throws Exception {
        User userFromUserRequest = buildUserFromUserRequest(baseUserRequest);
        when(userService.registerUser(baseUserRequest)).thenReturn(userFromUserRequest);

        String baseUserJson = """
                {
                    "username": "user",
                    "password": "1234",
                    "email": "user@mail.com",
                    "firstName": "John",
                    "lastName": "Doe",
                    "birthDate": "2024-09-24"
                }
                """;

        this.mockMvc
                .perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baseUserJson)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$.username").value(userFromUserRequest.getUsername()),
                        jsonPath("$.email").value(userFromUserRequest.getEmail()),
                        jsonPath("$.firstName").value(userFromUserRequest.getFirstName()),
                        jsonPath("$.lastName").value(userFromUserRequest.getLastName()),
                        jsonPath("$.birthDate").value("2024-09-24"));
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
