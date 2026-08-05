package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("저장된 사용자 이름이 존재하면 true를 반환한다")
    void existsByUsernameReturnsTrue() {
        // given
        User user = createUser(
                "user01",
                "user01@example.com"
        );

        userRepository.saveAndFlush(user);

        // when
        boolean exists = userRepository.existsByUsername("user01");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("저장되지 않은 사용자 이름이면 false를 반환한다")
    void existsByUsernameReturnsFalse() {
        // when
        boolean exists =
                userRepository.existsByUsername("unknown-user");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("저장된 이메일이 존재하면 true를 반환한다")
    void existsByEmailReturnsTrue() {
        // given
        User user = createUser(
                "user01",
                "user01@example.com"
        );

        userRepository.saveAndFlush(user);

        // when
        boolean exists =
                userRepository.existsByEmail("user01@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("저장되지 않은 이메일이면 false를 반환한다")
    void existsByEmailReturnsFalse() {
        // when
        boolean exists =
                userRepository.existsByEmail("unknown@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("사용자를 이름 오름차순으로 정렬하여 조회한다")
    void findAllWithUsernameAscendingSort() {
        // given
        userRepository.save(createUser(
                "charlie",
                "charlie@example.com"
        ));

        userRepository.save(createUser(
                "alice",
                "alice@example.com"
        ));

        userRepository.save(createUser(
                "bravo",
                "bravo@example.com"
        ));

        userRepository.flush();

        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "username"
        );

        // when
        var users = userRepository.findAll(sort);

        // then
        assertThat(users)
                .extracting(User::getUsername)
                .containsExactly(
                        "alice",
                        "bravo",
                        "charlie"
                );
    }

    @Test
    @DisplayName("사용자 목록을 페이지 단위로 조회한다")
    void findAllWithPagination() {
        // given
        userRepository.save(createUser(
                "user01",
                "user01@example.com"
        ));

        userRepository.save(createUser(
                "user02",
                "user02@example.com"
        ));

        userRepository.save(createUser(
                "user03",
                "user03@example.com"
        ));

        userRepository.flush();

        PageRequest pageRequest = PageRequest.of(
                0,
                2,
                Sort.by(
                        Sort.Direction.ASC,
                        "username"
                )
        );

        // when
        Page<User> result =
                userRepository.findAll(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();

        assertThat(result.getContent())
                .extracting(User::getUsername)
                .containsExactly(
                        "user01",
                        "user02"
                );
    }

    private User createUser(
            String username,
            String email
    ) {
        return new User(
                username,
                email,
                "password123",
                null
        );
    }
}