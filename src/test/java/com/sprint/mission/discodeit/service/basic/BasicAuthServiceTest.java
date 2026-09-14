package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SessionRegistry sessionRegistry;

    @InjectMocks
    private BasicAuthService authService;

    @Nested
    @DisplayName("사용자 권한 수정")
    class UpdateRole {

        @Test
        @DisplayName("사용자가 존재하면 권한을 수정하고 활성 세션을 만료")
        void update_success() {
            // given
            UUID userId = UUID.randomUUID();

            UserRoleUpdateRequest request = new UserRoleUpdateRequest(
                    userId,
                    Role.CHANNEL_MANAGER
            );

            User user = mock(User.class);

            UserDto principalUserDto = new UserDto(
                    userId,
                    "user1",
                    "user1@test.com",
                    null,
                    true,
                    Role.USER
            );

            DiscodeitUserDetails principal =
                    new DiscodeitUserDetails(
                            principalUserDto,
                            "encoded-password"
                    );

            SessionInformation sessionInformation =
                    mock(SessionInformation.class);

            UserDto expectedResponse = new UserDto(
                    userId,
                    "user1",
                    "user1@test.com",
                    null,
                    false,
                    Role.CHANNEL_MANAGER
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(user.getId())
                    .willReturn(userId);

            given(sessionRegistry.getAllPrincipals())
                    .willReturn(List.of(principal));

            given(sessionRegistry.getAllSessions(principal, false))
                    .willReturn(List.of(sessionInformation));

            given(userMapper.toDto(user, false))
                    .willReturn(expectedResponse);

            // when
            UserDto result = authService.updateRole(request);

            // then
            assertThat(result)
                    .isEqualTo(expectedResponse);

            assertThat(result.role())
                    .isEqualTo(Role.CHANNEL_MANAGER);

            assertThat(result.online())
                    .isFalse();

            then(userRepository).should()
                    .findById(userId);

            then(user).should()
                    .updateRole(Role.CHANNEL_MANAGER);

            then(sessionRegistry).should()
                    .getAllPrincipals();

            then(sessionRegistry).should()
                    .getAllSessions(principal, false);

            then(sessionInformation).should()
                    .expireNow();

            then(userMapper).should()
                    .toDto(user, false);
        }

        @Test
        @DisplayName("로그인 중이지 않은 사용자도 권한을 정상적으로 수정")
        void update_success_no_session() {
            // given
            UUID userId = UUID.randomUUID();

            UserRoleUpdateRequest request = new UserRoleUpdateRequest(
                    userId,
                    Role.CHANNEL_MANAGER
            );

            User user = mock(User.class);

            UserDto expectedResponse = new UserDto(
                    userId,
                    "user1",
                    "user1@test.com",
                    null,
                    false,
                    Role.CHANNEL_MANAGER
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(user.getId())
                    .willReturn(userId);

            given(sessionRegistry.getAllPrincipals())
                    .willReturn(List.of());

            given(userMapper.toDto(user, false))
                    .willReturn(expectedResponse);

            // when
            UserDto result = authService.updateRole(request);

            // then
            assertThat(result)
                    .isEqualTo(expectedResponse);

            then(user).should()
                    .updateRole(Role.CHANNEL_MANAGER);

            then(sessionRegistry).should()
                    .getAllPrincipals();

            then(userMapper).should()
                    .toDto(user, false);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외 발생")
        void update_fail_no_user() {
            // given
            UUID userId = UUID.randomUUID();

            UserRoleUpdateRequest request = new UserRoleUpdateRequest(
                    userId,
                    Role.CHANNEL_MANAGER
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.updateRole(request))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should()
                    .findById(userId);

            then(sessionRegistry).shouldHaveNoInteractions();

            then(userMapper).should(never())
                    .toDto(
                            org.mockito.ArgumentMatchers.any(User.class),
                            org.mockito.ArgumentMatchers.anyBoolean()
                    );
        }
    }
}