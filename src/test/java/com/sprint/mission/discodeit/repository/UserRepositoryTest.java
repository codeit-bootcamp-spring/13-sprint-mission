package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        User user = new User("박경석", "park@gmail.com", "0000", null, null);
        em.persist(user);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("유저이름 존재 확인 -존재함")
    void existsByUsername_true() {

        assertThat(userRepository.existsByUsername("박경석")).isTrue();

    }

    @Test
    @DisplayName("유저이름 존재 확인 - 존재하지 않음")
    void existsByUsername_false() {
        assertThat(userRepository.existsByUsername("김땡땡")).isFalse();
    }

    @Test
    @DisplayName("email 존재 확인 - 존재함")
    void existsByEmail_true() {
        assertThat(userRepository.existsByEmail("park@gmail.com")).isTrue();
    }

    @Test
    @DisplayName("email 존재 확인 - 존재하지 않음")
    void existsByEmail_false() {
        assertThat(userRepository.existsByEmail("none@gmail.com")).isFalse();
    }










}