package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("사용자 이름 존재 여부 조회")
    class ExistsByUsername {

        @Test
        @DisplayName("동일한 사용자 이름이 존재하면 true를 반환")
        void get_success() {
            // given
            User user = new User(
                    "user1",
                    "user1@example.com",
                    "password",
                    null,
                    Role.USER
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result = userRepository.existsByUsername("user1");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("동일한 사용자 이름이 없으면 false를 반환")
        void get_fail() {
            // when
            boolean result = userRepository.existsByUsername("unknown");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("이메일 존재 여부 조회")
    class ExistsByEmail {

        @Test
        @DisplayName("동일한 이메일이 존재하면 true를 반환")
        void get_success() {
            // given
            User user = new User(
                    "user1",
                    "user1@example.com",
                    "password",
                    null,
                    Role.USER
            );

            userRepository.saveAndFlush(user);

            // when
            boolean result = userRepository.existsByEmail("user1@example.com");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("동일한 이메일이 없으면 false를 반환")
        void get_fail() {
            // when
            boolean result =
                    userRepository.existsByEmail("unknown@example.com");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 이름으로 사용자 조회")
    class FindByUsername {

        @Test
        @DisplayName("사용자 이름이 일치하면 사용자를 반환")
        void get_success() {
            // given
            User user = new User(
                    "user1",
                    "user1@example.com",
                    "encoded-password",
                    null,
                    Role.USER
            );

            userRepository.saveAndFlush(user);

            // when
            Optional<User> result =
                    userRepository.findByUsername("user1");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername())
                    .isEqualTo("user1");
            assertThat(result.get().getEmail())
                    .isEqualTo("user1@example.com");
            assertThat(result.get().getRole())
                    .isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("사용자 이름이 존재하지 않으면 빈 Optional을 반환")
        void get_fail() {
            // when
            Optional<User> result =
                    userRepository.findByUsername("unknown");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("권한을 가진 사용자 존재 여부 조회")
    class ExistsByRole {

        @Test
        @DisplayName("해당 권한을 가진 사용자가 존재하면 true를 반환")
        void get_success() {
            // given
            User admin = new User(
                    "admin",
                    "admin@example.com",
                    "encoded-password",
                    null,
                    Role.ADMIN
            );

            userRepository.saveAndFlush(admin);

            // when
            boolean result =
                    userRepository.existsByRole(Role.ADMIN);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("해당 권한을 가진 사용자가 없으면 false를 반환")
        void get_fail() {
            // when
            boolean result =
                    userRepository.existsByRole(Role.ADMIN);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("전체 사용자 조회")
    class FindAll {

        @Test
        @DisplayName("저장된 모든 사용자를 조회")
        void get_success() {
            // given
            User firstUser = new User(
                    "user1",
                    "user1@example.com",
                    "password1",
                    null,
                    Role.USER
            );

            User secondUser = new User(
                    "user2",
                    "user2@example.com",
                    "password2",
                    null,
                    Role.CHANNEL_MANAGER
            );

            userRepository.saveAllAndFlush(
                    List.of(firstUser, secondUser)
            );

            // when
            List<User> result = userRepository.findAll();

            // then
            assertThat(result)
                    .hasSize(2);

            assertThat(result)
                    .extracting(User::getUsername)
                    .containsExactlyInAnyOrder(
                            "user1",
                            "user2"
                    );

            assertThat(result)
                    .extracting(User::getRole)
                    .containsExactlyInAnyOrder(
                            Role.USER,
                            Role.CHANNEL_MANAGER
                    );
        }

        @Test
        @DisplayName("저장된 사용자가 없으면 빈 목록을 반환")
        void get_fail() {
            // when
            List<User> result = userRepository.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }
}