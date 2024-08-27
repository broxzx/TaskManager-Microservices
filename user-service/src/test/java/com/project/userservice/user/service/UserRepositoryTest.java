package com.project.userservice.user.service;

import com.project.userservice.config.MongoDBBaseIntegrationTest;
import com.project.userservice.user.data.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class UserRepositoryTest extends MongoDBBaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User baseUser;

    @BeforeEach
    public void setUp() {
        this.baseUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("username")
                .email("email@email.com")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .birthDate(LocalDate.now())
                .isDeleted(false)
                .profilePictureUrl("https://example.com")
                .googleAccountId(UUID.randomUUID().toString())
                .build();
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void givenUserRepository_whenSaveUser_thenSuccess() {
        User createdUser = userRepository.save(baseUser);
        Optional<User> user = userRepository.findById(createdUser.getId());

        assertThat(user.isPresent()).isTrue();

        User obtainedUser = user.get();

        assertTrue(areUsersEqual(baseUser, obtainedUser));
    }

    @Test
    public void givenUserRepository_whenFindUserById_thenSuccess() {
        User savedUser = userRepository.save(baseUser);
        Optional<User> obtainedUserById = userRepository.findById(savedUser.getId());

        assertThat(obtainedUserById.isPresent()).isTrue();
    }

    @Test
    public void givenUserRepository_whenFindUserById_thenFailure() {
        User savedUser = userRepository.save(baseUser);
        String randomId = UUID.randomUUID().toString();
        Optional<User> obtainedUserById = userRepository.findById(randomId);

        assertThat(obtainedUserById.isPresent()).isFalse();
    }

    @Test
    public void givenUserRepository_whenUpdateUser_thenSuccess() {
        User savedUser = userRepository.save(baseUser);
        String newPassword = "newPassword";
        savedUser.setPassword(newPassword);

        User updatedUser = userRepository.save(savedUser);
        Optional<User> obtainedUser = userRepository.findById(updatedUser.getId());

        assertThat(obtainedUser.isPresent()).isTrue();

        User user = obtainedUser.get();
        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    public void givenUserRepository_whenDeleteUser_thenSuccess() {
        User savedUser = userRepository.save(baseUser);
        Optional<User> obtainedUserBeforeDelete = userRepository.findById(savedUser.getId());

        assertThat(obtainedUserBeforeDelete.isPresent()).isTrue();

        userRepository.deleteById(savedUser.getId());

        Optional<User> obtainedUserAfterDelete = userRepository.findById(savedUser.getId());
        assertThat(obtainedUserAfterDelete.isEmpty()).isTrue();

    }


    private boolean areUsersEqual(User expected, User actual) {
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
