package com.project.userservice.testUtils;

import com.project.userservice.user.data.dto.request.UserRequest;

import java.time.LocalDate;

public class UserUtils {

    public static UserRequest baseUserRequest() {
        return UserRequest.builder()
                .username("user")
                .password("1234")
                .email("user@mail.com")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(2024, 9, 24))
                .build();
    }

    public static String validBaseUserJson() {
        return """
                {
                    "username": "user",
                    "password": "1234",
                    "email": "user@mail.com",
                    "firstName": "John",
                    "lastName": "Doe",
                    "birthDate": "2024-09-24"
                }
                """;
    }

    public static String invalidBaseUserJson() {
        return """
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

}
