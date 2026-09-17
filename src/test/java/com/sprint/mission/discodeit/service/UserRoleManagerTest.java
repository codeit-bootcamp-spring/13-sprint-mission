package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.basic.UserRoleManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 역할 변경 컴포넌트 단위 테스트")
class UserRoleManagerTest {

    @InjectMocks
    UserRoleManager userRoleManager;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    SessionRegistry sessionRegistry;

    @Test
    @DisplayName("사용자가 존재하면 실제 엔티티의 역할을 변경한다")
    void updateRole_updatesActualUserRole_whenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = createUser();
        UserRoleUpdateCommand command = new UserRoleUpdateCommand(Role.CHANNEL_MANAGER);
        UserDto expected = userDto(userId, Role.CHANNEL_MANAGER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(expected);

        UserDto result = userRoleManager.updateRole(userId, command);

        assertThat(user.getRole()).isEqualTo(Role.CHANNEL_MANAGER);
        assertThat(result).isSameAs(expected);
        then(userRepository).should().findById(userId);
        then(userMapper).should().toDto(user);
    }

    @Test
    @DisplayName("역할을 변경하면 해당 사용자의 활성 세션만 만료한다")
    void updateRole_expiresOnlyTargetUserSessions() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User user = createUser();
        UserRoleUpdateCommand command = new UserRoleUpdateCommand(Role.CHANNEL_MANAGER);
        UserDto expected = userDto(userId, Role.CHANNEL_MANAGER);
        DiscodeitUserDetails targetPrincipal = new DiscodeitUserDetails(
                userDto(userId, Role.USER),
                "encodedPassword"
        );
        DiscodeitUserDetails otherPrincipal = new DiscodeitUserDetails(
                userDto(otherUserId, Role.USER),
                "encodedPassword"
        );
        SessionInformation targetSession = new SessionInformation(
                targetPrincipal,
                "target-session",
                new Date()
        );
        SessionInformation otherSession = new SessionInformation(
                otherPrincipal,
                "other-session",
                new Date()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(sessionRegistry.getAllPrincipals()).willReturn(List.of(targetPrincipal, otherPrincipal));
        given(sessionRegistry.getAllSessions(any(), eq(false))).willAnswer(invocation ->
                invocation.getArgument(0).equals(targetPrincipal)
                        ? List.of(targetSession)
                        : List.of(otherSession)
        );
        given(userMapper.toDto(user)).willReturn(expected);

        UserDto result = userRoleManager.updateRole(userId, command);

        assertThat(result).isSameAs(expected);
        assertThat(targetSession.isExpired()).isTrue();
        assertThat(otherSession.isExpired()).isFalse();
        then(sessionRegistry).should().getAllSessions(targetPrincipal, false);
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 예외를 발생시킨다")
    void updateRole_throwsException_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UserRoleUpdateCommand command = new UserRoleUpdateCommand(Role.ADMIN);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleManager.updateRole(userId, command))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(userId);
        then(userMapper).shouldHaveNoInteractions();
        then(sessionRegistry).shouldHaveNoInteractions();
    }

    private User createUser() {
        return new User(
                new UserCreateCommand("testUser", "encodedPassword", "test@example.com"),
                null
        );
    }

    private UserDto userDto(UUID userId, Role role) {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-11T10:00:00+09:00");
        return new UserDto(userId, "testUser", "test@example.com", null, false, role, now, now);
    }
}
