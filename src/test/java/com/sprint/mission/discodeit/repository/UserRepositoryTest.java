package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@DisplayName("UserRepository 슬라이스 테스트")
class UserRepositoryTest {

  @Autowired
  UserRepository repository;

  @Autowired
  TestEntityManager entityManager;

  @BeforeEach
  void setUp() {
    entityManager.persist(createUser("김김김", "asdf@test.com", "010-1111-1111"));
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("중복 확인 쿼리")
  class ExistsQuery {

    @Test
    @DisplayName("등록된 이메일, 사용자명, 전화번호는 true를 반환한다")
    void findExistingUser() {
      assertThat(repository.existsByEmail("asdf@test.com")).isTrue();
      assertThat(repository.existsByUsername("김김김")).isTrue();
    }

    @Test
    @DisplayName("등록되지 않은 값은 false를 반환한다")
    void rejectUnknownUser() {
      assertThat(repository.existsByEmail("not@test.com")).isFalse();
      assertThat(repository.existsByUsername("김없음")).isFalse();
    }
  }

  private User createUser(String username, String email, String phoneNumber) {
    return User.builder()
        .username(username)
        .email(email)
        .phoneNumber(phoneNumber)
        .password("password")
        .role(Role.USER)
        .build();
  }
}
