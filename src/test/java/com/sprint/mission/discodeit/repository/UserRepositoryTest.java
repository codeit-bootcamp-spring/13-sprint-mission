package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_사용자가_존재하면_조회한다() {
        User user = new User("codeit", "codeit@example.com", "password123");
        userRepository.saveAndFlush(user);

        Optional<User> result = userRepository.findByUsername("codeit");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("codeit");
        assertThat(result.get().getEmail()).isEqualTo("codeit@example.com");
    }

    @Test
    void findByUsername_사용자가_없으면_빈_값을_반환한다() {
        Optional<User> result = userRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }
}