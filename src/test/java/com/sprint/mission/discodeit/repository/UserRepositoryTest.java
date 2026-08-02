package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @org.springframework.beans.factory.annotation.Autowired
  private UserRepository userRepository;

  @org.springframework.beans.factory.annotation.Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("findByUserName 성공 - 존재하는 username이면 User를 반환한다")
  void findByUserName_success() {
    // given
    User user = new User("testuser", "password123!", "test@example.com");
    entityManager.persist(user);
    entityManager.flush();

    // when
    Optional<User> result = userRepository.findByUserName("testuser");

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("findByUserName 실패 - 존재하지 않는 username이면 빈 Optional을 반환한다")
  void findByUserName_fail_notFound() {
    // when
    Optional<User> result = userRepository.findByUserName("nonexistent");

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("findByEmail 성공 - 존재하는 email이면 User를 반환한다")
  void findByEmail_success() {
    // given
    User user = new User("emailuser", "password123!", "email@example.com");
    entityManager.persist(user);
    entityManager.flush();

    // when
    Optional<User> result = userRepository.findByEmail("email@example.com");

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getUserName()).isEqualTo("emailuser");
  }

  @Test
  @DisplayName("findByEmail 실패 - 존재하지 않는 email이면 빈 Optional을 반환한다")
  void findByEmail_fail_notFound() {
    // when
    Optional<User> result = userRepository.findByEmail("nobody@example.com");

    // then
    assertThat(result).isEmpty();
  }
}