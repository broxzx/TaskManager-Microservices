package com.project.userservice.user.service;

import com.project.userservice.UserServiceApplication;
import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = UserServiceApplication.class)
@Slf4j
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private UserRequest baseUserRequest;
    private User baseUser;

    @BeforeEach
    void setUp() {
        this.baseUserRequest = UserRequest.builder()
                .username("username")
                .password("password")
                .email("email@email.com")
                .firstName("firstName")
                .lastName("lastName")
                .birthDate(LocalDate.now())
                .build();

        this.baseUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("username")
                .password("password")
                .email("email@email.com")
                .firstName("firstName")
                .lastName("lastName")
                .birthDate(LocalDate.now())
                .isDeleted(false)
                .profilePictureUrl("https://example.com")
                .googleAccountId(UUID.randomUUID().toString())
                .build();
    }

    @Test
    void givenUserId_whenGetUser_thenReturnUser() {
        Optional<User> optionalBaseUser = Optional.of(baseUser);

        when(userRepository.findById(any(String.class))).thenReturn(optionalBaseUser);

        User obtainedUserEntity = userService.getUserEntityById(baseUser.getId());

        assertNotNull(obtainedUserEntity);
        assertThat(obtainedUserEntity.getId()).isEqualTo(baseUser.getId());
        verify(userRepository, times(1)).findById(baseUser.getId());
    }

    @Test
    void givenUserId_whenGetUser_thenFailure() {
        String id = UUID.randomUUID().toString();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.getUserEntityById(id));

        assertThat(exception.getMessage()).isEqualTo("user with id '%s' is not found".formatted(id));
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void givenUser_whenRegisterUser_thenReturnUser() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(baseUser);

        User savedUser = userService.registerUser(baseUserRequest);

        assertNotNull(savedUser);
        assertThat(areUserEqual(baseUser, savedUser)).isTrue();

        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userRepository, times(1)).findByEmail(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserEntity() {
    }

    @Test
    void getUserEntityByUsername() {
    }

    @Test
    void changePasswordWithNewPassword() {
    }

    @Test
    void processGrantCode() {
    }

    @Test
    void getUserIdByToken() {
    }

    @Test
    void checkUserExists() {
    }

    private boolean areUserEqual(User expected, User actual) {
        return expected.getId().equals(actual.getId()) &&
                expected.getUsername().equals(actual.getUsername()) &&
                expected.getPassword().equals(actual.getPassword()) &&
                expected.getEmail().equals(actual.getEmail()) &&
                expected.getEmailVerified() == actual.getEmailVerified() &&
                expected.getFirstName().equals(actual.getFirstName()) &&
                expected.getLastName().equals(actual.getLastName()) &&
                expected.getBirthDate().equals(actual.getBirthDate()) &&
                expected.getProfilePictureUrl().equals(actual.getProfilePictureUrl()) &&
                expected.getGoogleAccountId().equals(actual.getGoogleAccountId()) &&
                expected.isCalendarSyncEnabled() == actual.isCalendarSyncEnabled() &&
                expected.getCreatedDate().truncatedTo(ChronoUnit.MILLIS).equals(actual.getCreatedDate().truncatedTo(ChronoUnit.MILLIS)) &&
                expected.getLastLoginDate().truncatedTo(ChronoUnit.MILLIS).equals(actual.getLastLoginDate().truncatedTo(ChronoUnit.MILLIS)) &&
                Objects.equals(expected.getTaskCompletionRate(), actual.getTaskCompletionRate()) &&
                expected.getAchievements().equals(actual.getAchievements()) &&
                Objects.equals(expected.getPoints(), actual.getPoints()) &&
                Objects.equals(expected.getLevel(), actual.getLevel()) &&
                expected.getIsDeleted() == actual.getIsDeleted() &&
                expected.getRoles().equals(actual.getRoles());
    }
}
