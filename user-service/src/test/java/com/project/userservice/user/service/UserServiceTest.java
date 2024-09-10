package com.project.userservice.user.service;

import com.project.userservice.UserServiceApplication;
import com.project.userservice.exception.EntityNotFoundException;
import com.project.userservice.exception.PasswordNotMatch;
import com.project.userservice.exception.ResetPasswordTokenIncorrectException;
import com.project.userservice.exception.UserAlreadyExistsException;
import com.project.userservice.model.ChangePasswordDto;
import com.project.userservice.user.data.User;
import com.project.userservice.user.data.dto.request.UserRequest;
import com.project.userservice.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @MockBean
    public JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private UserRequest baseUserRequest;
    private User baseUser;
    private String randomToken;

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
                .password(passwordEncoder.encode("password"))
                .email("email@email.com")
                .firstName("firstName")
                .lastName("lastName")
                .birthDate(LocalDate.now())
                .isDeleted(false)
                .profilePictureUrl("https://example.com")
                .googleAccountId(UUID.randomUUID().toString())
                .build();

        this.randomToken = UUID.randomUUID().toString();
    }

    @Test
    void givenUserId_whenGetUser_thenReturnUser() {
        Optional<User> optionalBaseUser = Optional.of(baseUser);

        when(userRepository.findById(any(String.class))).thenReturn(optionalBaseUser);

        User obtainedUserEntity = userService.getUserById(baseUser.getId());

        assertNotNull(obtainedUserEntity);
        assertThat(obtainedUserEntity.getId()).isEqualTo(baseUser.getId());
        verify(userRepository, times(1)).findById(baseUser.getId());
    }

    @Test
    void givenUserId_whenGetUser_thenFailure() {
        String id = UUID.randomUUID().toString();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.getUserById(id));

        assertThat(exception.getMessage()).isEqualTo("user with id '%s' is not found".formatted(id));
        verify(userRepository, times(1)).findById(any(String.class));
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
    void givenUser_whenRegisterUserWithUsernameDuplicateInDB_thenFailure() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(baseUser));
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(baseUserRequest));

        assertThat(exception.getMessage()).isEqualTo("user with username '%s' already exists".formatted(baseUser.getUsername()));

        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userRepository, times(0)).findByEmail(any(String.class));
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void givenUser_whenRegisterUserWithEmailDuplicateInDB_thenFailure() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(baseUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(baseUserRequest));

        assertThat(exception.getMessage()).isEqualTo("user with email '%s' already exists".formatted(baseUser.getEmail()));

        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userRepository, times(1)).findByEmail(any(String.class));
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void givenUserService_whenUpdateUserEntity_thenSuccess() {
        when(jwtUtils.extractTokenFromHeader(any(String.class))).thenReturn(randomToken);
        when(jwtUtils.getUserIdByToken(randomToken)).thenReturn(baseUser.getId());
        when(userRepository.findById(baseUser.getId())).thenReturn(Optional.of(baseUser));

        userService.updateUserEntity(randomToken, baseUserRequest);

        User updatedUser = userService.getUserById(baseUser.getId());

        assertThat(updatedUser.getUsername()).isEqualTo(baseUser.getUsername());
        assertThat(updatedUser.getEmail()).isEqualTo(baseUser.getEmail());
        assertThat(areUserEqual(baseUser, updatedUser)).isTrue();
    }

    @Test
    void givenUserRequest_whenUpdateUserEntityWithInvalidToken_thenFailure() {
        final String dummyTokenValue = " ";
        when(jwtUtils.extractTokenFromHeader(any(String.class))).thenThrow(new ResetPasswordTokenIncorrectException("reset password token is invalid"));

        assertThrows(ResetPasswordTokenIncorrectException.class, () -> userService.updateUserEntity(dummyTokenValue, baseUserRequest));

        verify(jwtUtils, times(1)).extractTokenFromHeader(any(String.class));
    }

    @Test
    void givenUserRequest_whenUpdateUserEntityWithIncorrectFields_thenFailure() {
        UserRequest badUserRequest = UserRequest.builder()
                .username("")
                .password("")
                .email("")
                .firstName("")
                .lastName("")
                .birthDate(LocalDate.now())
                .build();

        when(jwtUtils.extractTokenFromHeader(any(String.class))).thenReturn(randomToken);
        when(jwtUtils.getUserIdByToken(randomToken)).thenReturn(baseUser.getId());
        when(userRepository.findById(baseUser.getId())).thenReturn(Optional.of(baseUser));
        when(userRepository.save(any(User.class))).thenReturn(baseUser);

        User updatedUserEntity = userService.updateUserEntity("dummyToken", badUserRequest);

        assertThat(areUserEqual(baseUser, updatedUserEntity)).isTrue();
    }

    @Test
    void givenUserService_whenFindUserById_thenSuccess() {
        when(userRepository.findById(baseUser.getId())).thenReturn(Optional.of(baseUser));

        User obtainedUserEntity = userService.getUserById(baseUser.getId());

        assertThat(obtainedUserEntity.getId()).isEqualTo(baseUser.getId());
        verify(userRepository, times(1)).findById(baseUser.getId());
    }

    @Test
    void givenUserService_whenFindUserById_thenFailure() {
        when(userRepository.findById(any(String.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(baseUser.getId()));

        verify(userRepository, times(1)).findById(any(String.class));
    }

    @Test
    void givenUserService_whenChangePasswordWithNewPassword_thenSuccess() {
        final String baseUserEmail = "email@email.com";
        final String passwordBeforeChanging = baseUser.getPassword();

        when(jwtUtils.getEmailFromResetPasswordToken(randomToken)).thenReturn(baseUserEmail);
        when(userRepository.findByEmail(baseUserEmail)).thenReturn(Optional.of(baseUser));
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("newPassword", "newPassword");

        userService.changePasswordWithNewPassword(randomToken, changePasswordDto);

        assertThat(baseUser.getPassword()).isNotEqualTo(passwordBeforeChanging);

        verify(userRepository, times(1)).findByEmail(baseUserEmail);
        verify(jwtUtils, times(1)).getEmailFromResetPasswordToken(randomToken);
    }

    @Test
    void givenUserService_whenChangePasswordWithIncorrectPassword_thenFailure() {
        final String baseUserEmail = "email@email.com";

        when(jwtUtils.getEmailFromResetPasswordToken(randomToken)).thenReturn(baseUserEmail);
        when(userRepository.findByEmail(baseUserEmail)).thenReturn(Optional.of(baseUser));
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("newPassword", "drowssaPwen");

        PasswordNotMatch passwordNotMatch = assertThrows(PasswordNotMatch.class, () -> userService.changePasswordWithNewPassword(randomToken, changePasswordDto));

        assertThat(passwordNotMatch.getMessage()).isEqualTo("passwords don't match");
        verify(userRepository, times(1)).findByEmail(baseUserEmail);
        verify(jwtUtils, times(1)).getEmailFromResetPasswordToken(randomToken);
    }

    @Test
    void givenUserService_whenGetUserIdByWithCorrectToken_thenSuccess() {
        final String randomToken = "Authorization " + UUID.randomUUID();

        when(jwtUtils.getUserIdByToken(randomToken)).thenReturn(baseUser.getId());

        String userId = userService.getUserIdByToken(randomToken);

        assertThat(userId).isEqualTo(baseUser.getId());
    }

    @Test
    void givenUserService_whenGetUserIdByWithIncorrectToken_thenFailure() {
        when(jwtUtils.getUserIdByToken(randomToken)).thenReturn(null);

        String userId = userService.getUserIdByToken(randomToken);

        assertThat(userId).isNotEqualTo(baseUser.getId());
    }

    @Test
    void givenUserService_whenCheckUserExists_thenSuccess() {
        when(userRepository.existsById(baseUser.getId())).thenReturn(true);

        boolean exists = userService.checkUserExists(baseUser.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void givenUserService_whenCheckUserExists_thenFailure() {
        when(userRepository.existsById(baseUser.getId())).thenReturn(false);

        boolean exists = userService.checkUserExists(baseUser.getId());

        assertThat(exists).isFalse();
    }

    private boolean areUserEqual(User expected, User actual) {
        return expected.getId().equals(actual.getId()) &&
                expected.getUsername().equals(actual.getUsername()) &&
                expected.getPassword().equals(actual.getPassword()) &&
                expected.getEmail().equals(actual.getEmail()) &&
                expected.isEmailVerified() == actual.isEmailVerified() &&
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
                expected.isDeleted() == actual.isDeleted() &&
                expected.getRoles().equals(actual.getRoles());
    }
}
