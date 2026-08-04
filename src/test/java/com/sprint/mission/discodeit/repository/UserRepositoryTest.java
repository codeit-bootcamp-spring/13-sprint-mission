package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("username으로 사용자를 조회한다")
    void findByName_success() {
        User user = new User("tester", "tester@example.com", "password", null);
        userRepository.save(user);

        Optional<User> result = userRepository.findByName("tester");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("tester");
        assertThat(result.get().getEmail()).isEqualTo("tester@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 username이면 빈 Optional을 반환한다")
    void findByName_notFound() {
        Optional<User> result = userRepository.findByName("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("email로 사용자를 조회한다")
    void findByEmail_success() {
        User user = new User("tester", "tester@example.com", "password", null);
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("tester@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("tester@example.com");
    }

    @Test
    @DisplayName("사용자 목록을 username 기준 오름차순으로 페이징 조회한다")
    void findAll_withPagingAndSort() {
        User charlie = new User("charlie", "charlie@example.com", "password", null);
        User alpha = new User("alpha", "alpha@example.com", "password", null);
        User bravo = new User("bravo", "bravo@example.com", "password", null);
        userRepository.saveAll(List.of(charlie, alpha, bravo));

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("name").ascending());

        Page<User> result = userRepository.findAll(pageRequest);

        assertThat(result.getContent())
                .extracting(User::getName)
                .containsExactly("alpha", "bravo");
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}