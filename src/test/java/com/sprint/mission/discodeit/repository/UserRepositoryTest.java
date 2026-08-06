package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.*;

import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest // JPA 계층 관련 빈만 로딩한다
@ActiveProfiles("test") // 테스트 실행 간 test 프로파일 활성화
@EnableJpaAuditing // JPA Audit 기능 활성화
@DisplayName("UserRepository 슬라이스 테스트")
public class UserRepositoryTest {

  @Autowired
  UserRepository userRepository;

  @Autowired
  TestEntityManager entityManager;

  private User persist(String username, String email, String password) {
    User user = new User(username, email, password, null);
    entityManager.persist(user);
    entityManager.flush();
    entityManager.clear();
    return user;
  }

  @Nested
  @DisplayName("직접 작성한 @Query")
  class CustomQueries {

    @Test
    @DisplayName("existByEmail - 특정 이메일이 존재한다면 true를 반환한다")
    void 존재하는_이메일() {
      // given
      persist("사용자", "user@icloud.com", "Abcd1234!");
      // when & then
      boolean existed = userRepository.existsByEmail("user@icloud.com");
      assertThat(existed).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - 특정 이메일이 존재하지 않는다면 false를 반환한다")
    void 존재하지_않는_이메일() {
      // when
      boolean existed = userRepository.existsByEmail("none@icloud.com");
      // then
      assertThat(existed).isFalse();
    }

    @Test
    @DisplayName("existsByUsername - 특정 이름이 존재하면 true를 반환한다")
    void 존재하는_사용자_이름() {
      // given
      persist("사용자", "user@icloud.com", "Abcd1234!");
      // when
      boolean existed = userRepository.existsByUsername("사용자");
      // then
      assertThat(existed).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - 특정 이름이 존재하지 않는다면 false를 반환한다")
    void 존재하지_않는_사용자_이름() {
      // when
      boolean existed = userRepository.existsByUsername("none");
      // then
      assertThat(existed).isFalse();
    }
  }
}
