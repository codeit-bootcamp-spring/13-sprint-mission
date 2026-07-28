package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("사용자 이름으로 사용자를 조회할 수 있다")
  void 사용자_이름으로_조회_성공() {
    // given
    User user = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    userRepository.save(user);

    // when
    Optional<User> result =
        userRepository.findByUsername("testUser");

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getUsername())
        .isEqualTo("testUser");
    assertThat(result.get().getEmail())
        .isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("존재하지 않는 사용자 이름으로 조회하면 빈 결과를 반환한다")
  void 사용자_이름으로_조회_실패_존재하지_않음() {
    // when
    Optional<User> result =
        userRepository.findByUsername("missingUser");

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("이메일로 사용자를 조회할 수 있다")
  void 이메일로_조회_성공() {
    // given
    User user = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    userRepository.save(user);

    // when
    Optional<User> result =
        userRepository.findByEmail("test@test.com");

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail())
        .isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 조회하면 빈 결과를 반환한다")
  void 이메일로_조회_실패_존재하지_않음() {
    // when
    Optional<User> result =
        userRepository.findByEmail("missing@test.com");

    // then
    assertThat(result).isEmpty();
  }
}