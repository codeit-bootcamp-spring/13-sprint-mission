package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 슬라이스 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("username 존재 여부 확인")
    class ExistsByUsername {

        @Test
        @DisplayName("동일한 username의 사용자가 존재하면 true를 반환한다")
        void success() {
            // given
            User user = new User(
                    "tester",
                    "tester@test.com",
                    "password"
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result = userRepository.existsByUsername("tester");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("동일한 username의 사용자가 존재하지 않으면 false를 반환한다")
        void notFound() {
            // given
            User user = new User(
                    "tester",
                    "tester@test.com",
                    "password"
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result = userRepository.existsByUsername("unknown");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("email 존재 여부 확인")
    class ExistsByEmail {

        @Test
        @DisplayName("동일한 email의 사용자가 존재하면 true를 반환한다")
        void success() {
            // given
            User user = new User(
                    "tester",
                    "tester@test.com",
                    "password"
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result = userRepository.existsByEmail("tester@test.com");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("동일한 email의 사용자가 존재하지 않으면 false를 반환한다")
        void notFound() {
            // given
            User user = new User(
                    "tester",
                    "tester@test.com",
                    "password"
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result = userRepository.existsByEmail("unknown@test.com");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("username으로 사용자 조회")
    class FindByUsername {

        @Test
        @DisplayName("존재하는 username으로 조회하면 사용자를 반환한다")
        void success() {
            // given
            User savedUser = userRepository.saveAndFlush(
                    new User(
                            "tester",
                            "tester@test.com",
                            "password"
                    )
            );

            // when
            User result = userRepository.findByUsername("tester");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedUser.getId());
            assertThat(result.getUsername()).isEqualTo("tester");
            assertThat(result.getEmail()).isEqualTo("tester@test.com");
            assertThat(result.getPassword()).isEqualTo("password");
            assertThat(result.getProfile()).isNull();
            assertThat(result.getProfileId()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 username으로 조회하면 null을 반환한다")
        void notFound() {
            // given
            userRepository.saveAndFlush(
                    new User(
                            "tester",
                            "tester@test.com",
                            "password"
                    )
            );

            // when
            User result = userRepository.findByUsername("unknown");

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("email로 사용자 조회")
    class FindByEmail {

        @Test
        @DisplayName("존재하는 email로 조회하면 사용자를 반환한다")
        void success() {
            // given
            User savedUser = userRepository.saveAndFlush(
                    new User(
                            "tester",
                            "tester@test.com",
                            "password"
                    )
            );

            // when
            User result = userRepository.findByEmail("tester@test.com");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedUser.getId());
            assertThat(result.getUsername()).isEqualTo("tester");
            assertThat(result.getEmail()).isEqualTo("tester@test.com");
            assertThat(result.getPassword()).isEqualTo("password");
            assertThat(result.getProfile()).isNull();
            assertThat(result.getProfileId()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 email로 조회하면 null을 반환한다")
        void notFound() {
            // given
            userRepository.saveAndFlush(
                    new User(
                            "tester",
                            "tester@test.com",
                            "password"
                    )
            );

            // when
            User result = userRepository.findByEmail("unknown@test.com");

            // then
            assertThat(result).isNull();
        }
    }
}