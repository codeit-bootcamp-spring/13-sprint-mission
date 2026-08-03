package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        return new User(
                "홍길동",
                "hong12@test.com",
                "12345"
        );
    }

    @Test
    @DisplayName("사용자 이름으로 사용자를 조회한다.")
    void findByUsername_success() {
        // given
        User user = createUser();
        userRepository.saveAndFlush(user);

        // when
        var result = userRepository.findByUsername("홍길동");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("hong12@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 이름으로 조회하면 빈 Optional을 반환한다")
    void findByUsername_notFound() {
        // when
        var result =
                userRepository.findByUsername("홍두깨");

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("이메일로 사용자를 조회를 할 수 있다.")
    void findByEmail_success() {
        //given
        User user = createUser();
        userRepository.saveAndFlush(user);
        
        //when
        var result = userRepository.findByEmail("hong12@test.com");
        
        //then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername())
                .isEqualTo("홍길동");
    }
    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional 반환")
    void findEmail_notFound() {
        // when
        var result = userRepository.findByEmail("unknown@test.com");
        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이미 존재하는 사용자 이름이면 ture룰 반환한다.")
    void existsByUsername_true() {
        // given
        userRepository.saveAndFlush(createUser());

        // when
        boolean result = userRepository.existsByUsername("홍길동");

        // then
        assertThat(result).isTrue();

    }

}
