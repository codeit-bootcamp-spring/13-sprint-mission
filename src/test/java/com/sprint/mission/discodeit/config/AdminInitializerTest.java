package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("AdminInitializer 테스트")
class AdminInitializerTest {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {

        userRepository =
                mock(UserRepository.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        adminInitializer =
                new AdminInitializer(
                        userRepository,
                        passwordEncoder
                );
    }

    @Test
    @DisplayName("ADMIN 계정이 없으면 애플리케이션 초기화 시 ADMIN 계정을 생성한다")
    void should_CreateAdmin_when_AdminDoesNotExist()
            throws Exception {

        // given
        given(
                userRepository.existsByRole(Role.ADMIN)
        ).willReturn(false);

        given(
                passwordEncoder.encode("admin1234")
        ).willReturn("$2a$10$encodedAdminPassword");

        given(
                userRepository.save(any(User.class))
        ).willAnswer(
                invocation -> invocation.getArgument(0)
        );

        // when
        adminInitializer.run(
                new DefaultApplicationArguments(
                        new String[0]
                )
        );

        // then
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedAdmin =
                userCaptor.getValue();

        assertThat(savedAdmin.getUsername())
                .isEqualTo("admin");

        assertThat(savedAdmin.getEmail())
                .isEqualTo("admin@discodeit.com");

        assertThat(savedAdmin.getPassword())
                .isEqualTo("$2a$10$encodedAdminPassword");

        assertThat(savedAdmin.getRole())
                .isEqualTo(Role.ADMIN);

        verify(passwordEncoder)
                .encode("admin1234");
    }

    @Test
    @DisplayName("ADMIN 계정이 이미 존재하면 새로운 ADMIN 계정을 생성하지 않는다")
    void should_NotCreateAdmin_when_AdminAlreadyExists()
            throws Exception {

        // given
        given(
                userRepository.existsByRole(Role.ADMIN)
        ).willReturn(true);

        // when
        adminInitializer.run(
                new DefaultApplicationArguments(
                        new String[0]
                )
        );

        // then
        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(any());
    }
}