package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.TestJpaConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User("testuser", "test@test.com", "password1234", null);
        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user, Instant.now());
        userStatusRepository.save(userStatus);

        savedUser = user;
    }


    @Test
    @DisplayName("findByUsername 성공 - 존재하는 username으로 조회하면 User를 반환한다")
    void findByUsername_성공() {
        // when
        Optional<User> result = userRepository.findByUsername("testuser");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("findByUsername 실패 - 존재하지 않는 username이면 empty를 반환한다")
    void findByUsername_실패() {
        // when
        Optional<User> result = userRepository.findByUsername("notexist");

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("existsByEmail - 존재하는 이메일이면 true를 반환한다")
    void existsByEmail_true() {
        // when
        boolean result = userRepository.existsByEmail("test@test.com");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - 존재하지 않는 이메일이면 false를 반환한다")
    void existsByEmail_false() {
        // when
        boolean result = userRepository.existsByEmail("notexist@test.com");

        // then
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("findAllWithProfileAndStatus - UserStatus가 있는 User만 반환한다")
    void findAllWithProfileAndStatus_성공() {

        // when: JOIN FETCH로 profile과 status를 한 번에 로드
        List<User> result = userRepository.findAllWithProfileAndStatus();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(0).getStatus()).isNotNull();
    }

    @Test
    @DisplayName("findAllWithProfileAndStatus - UserStatus가 없는 User는 반환하지 않는다")
    void findAllWithProfileAndStatus_상태없는유저제외() {
        User userWithoutStatus = new User("nostatususer", "nostatus@test.com", "password", null);
        userRepository.save(userWithoutStatus);
        // UserStatus 저장 안 함

        // when
        List<User> result = userRepository.findAllWithProfileAndStatus();

        // then: 기존 1명만 반환
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }
}