package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 슬라이스 테스트")
class UserRepositoryTest {

    private static final String USERNAME = "tester";
    private static final String EMAIL = "tester@test.com";
    private static final String PASSWORD = "password";

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("username 존재 여부 확인")
    class ExistsByUsername {

        @Test
        @DisplayName("동일한 username의 사용자가 존재하면 true를 반환한다")
        void should_ReturnTrue_when_UserWithUsernameExists() {
            // given
            User user = createUser(
                    USERNAME,
                    EMAIL,
                    PASSWORD
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result =
                    userRepository.existsByUsername(USERNAME);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("동일한 username의 사용자가 존재하지 않으면 false를 반환한다")
        void should_ReturnFalse_when_UserWithUsernameDoesNotExist() {
            // given
            User user = createUser(
                    USERNAME,
                    EMAIL,
                    PASSWORD
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result =
                    userRepository.existsByUsername("unknown");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("email 존재 여부 확인")
    class ExistsByEmail {

        @Test
        @DisplayName("동일한 email의 사용자가 존재하면 true를 반환한다")
        void should_ReturnTrue_when_UserWithEmailExists() {
            // given
            User user = createUser(
                    USERNAME,
                    EMAIL,
                    PASSWORD
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result =
                    userRepository.existsByEmail(EMAIL);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("동일한 email의 사용자가 존재하지 않으면 false를 반환한다")
        void should_ReturnFalse_when_UserWithEmailDoesNotExist() {
            // given
            User user = createUser(
                    USERNAME,
                    EMAIL,
                    PASSWORD
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result =
                    userRepository.existsByEmail(
                            "unknown@test.com"
                    );

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("username으로 사용자 조회")
    class FindByUsername {

        @Test
        @DisplayName("존재하는 username으로 조회하면 사용자를 반환한다")
        void should_ReturnUser_when_UsernameExists() {
            // given
            User savedUser = userRepository.saveAndFlush(
                    createUser(
                            USERNAME,
                            EMAIL,
                            PASSWORD
                    )
            );

            // when
            Optional<User> result =
                    userRepository.findByUsername(USERNAME);

            // then
            assertThat(result).isPresent();

            User foundUser = result.get();

            assertThat(foundUser.getId())
                    .isEqualTo(savedUser.getId());

            assertThat(foundUser.getUsername())
                    .isEqualTo(USERNAME);

            assertThat(foundUser.getEmail())
                    .isEqualTo(EMAIL);

            assertThat(foundUser.getPassword())
                    .isEqualTo(PASSWORD);

            assertThat(foundUser.getProfile()).isNull();
            assertThat(foundUser.getProfileId()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 username으로 조회하면 빈 Optional을 반환한다")
        void should_ReturnEmptyOptional_when_UsernameDoesNotExist() {
            // given
            userRepository.saveAndFlush(
                    createUser(
                            USERNAME,
                            EMAIL,
                            PASSWORD
                    )
            );

            // when
            Optional<User> result =
                    userRepository.findByUsername("unknown");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("email로 사용자 조회")
    class FindByEmail {

        @Test
        @DisplayName("존재하는 email로 조회하면 사용자를 반환한다")
        void should_ReturnUser_when_EmailExists() {
            // given
            User savedUser = userRepository.saveAndFlush(
                    createUser(
                            USERNAME,
                            EMAIL,
                            PASSWORD
                    )
            );

            // when
            Optional<User> result =
                    userRepository.findByEmail(EMAIL);

            // then
            assertThat(result).isPresent();

            User foundUser = result.get();

            assertThat(foundUser.getId())
                    .isEqualTo(savedUser.getId());

            assertThat(foundUser.getUsername())
                    .isEqualTo(USERNAME);

            assertThat(foundUser.getEmail())
                    .isEqualTo(EMAIL);

            assertThat(foundUser.getPassword())
                    .isEqualTo(PASSWORD);

            assertThat(foundUser.getProfile()).isNull();
            assertThat(foundUser.getProfileId()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 email로 조회하면 빈 Optional을 반환한다")
        void should_ReturnEmptyOptional_when_EmailDoesNotExist() {
            // given
            userRepository.saveAndFlush(
                    createUser(
                            USERNAME,
                            EMAIL,
                            PASSWORD
                    )
            );

            // when
            Optional<User> result =
                    userRepository.findByEmail(
                            "unknown@test.com"
                    );

            // then
            assertThat(result).isEmpty();
        }
    }

    private User createUser(
            String username,
            String email,
            String password
    ) {

        UserData userData = new UserData(
                username,
                email,
                password
        );

        return new User(userData);
    }
}