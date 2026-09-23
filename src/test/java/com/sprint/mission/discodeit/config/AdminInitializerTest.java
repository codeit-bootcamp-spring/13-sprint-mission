package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {
        adminInitializer = new AdminInitializer(
                userRepository,
                userStatusRepository,
                passwordEncoder
        );

        ReflectionTestUtils.setField(
                adminInitializer,
                "username",
                "admin"
        );
        ReflectionTestUtils.setField(
                adminInitializer,
                "email",
                "admin@discodeit.com"
        );
        ReflectionTestUtils.setField(
                adminInitializer,
                "password",
                "admin1234"
        );
    }

    @Test
    void ADMIN_계정이_없으면_생성한다() {
        given(userRepository.existsByRole(UserRole.ADMIN))
                .willReturn(false);
        given(passwordEncoder.encode("admin1234"))
                .willReturn("encoded-admin-password");
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        adminInitializer.run(mock(ApplicationArguments.class));

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserStatus> statusCaptor =
                ArgumentCaptor.forClass(UserStatus.class);

        then(userRepository).should().save(userCaptor.capture());
        then(userStatusRepository)
                .should()
                .save(statusCaptor.capture());

        User savedAdmin = userCaptor.getValue();

        assertThat(savedAdmin.getUsername()).isEqualTo("admin");
        assertThat(savedAdmin.getEmail())
                .isEqualTo("admin@discodeit.com");
        assertThat(savedAdmin.getPassword())
                .isEqualTo("encoded-admin-password");
        assertThat(savedAdmin.getRole())
                .isEqualTo(UserRole.ADMIN);
        assertThat(statusCaptor.getValue().getUser())
                .isSameAs(savedAdmin);
    }

    @Test
    void ADMIN_계정이_이미_있으면_생성하지_않는다() {
        given(userRepository.existsByRole(UserRole.ADMIN))
                .willReturn(true);

        adminInitializer.run(mock(ApplicationArguments.class));

        then(userRepository)
                .should(never())
                .save(any(User.class));
        then(userStatusRepository)
                .shouldHaveNoInteractions();
        then(passwordEncoder)
                .shouldHaveNoInteractions();
    }
}