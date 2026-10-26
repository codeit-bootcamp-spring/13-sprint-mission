package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.JwtRegistry;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BasicAuthService authService;

    @Nested
    @DisplayName("사용자 권한 수정")
    class UpdateRole {

        @Test
        @DisplayName("사용자가 존재하면 권한을 수정하고 JWT 정보를 무효화하며 이벤트를 발행")
        void update_success() {
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

            given(user.getRole())
                    .willReturn(Role.USER);

            given(jwtRegistry.hasActiveJwtInformationByUserId(userId))
                    .willReturn(false);

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

            then(jwtRegistry).should()
                    .invalidateJwtInformationByUserId(userId);

            then(jwtRegistry).should()
                    .hasActiveJwtInformationByUserId(userId);

            ArgumentCaptor<RoleUpdatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(RoleUpdatedEvent.class);

            then(eventPublisher).should()
                    .publishEvent(eventCaptor.capture());

            RoleUpdatedEvent event = eventCaptor.getValue();

            assertThat(event.userId())
                    .isEqualTo(userId);

            assertThat(event.oldRole())
                    .isEqualTo(Role.USER);

            assertThat(event.newRole())
                    .isEqualTo(Role.CHANNEL_MANAGER);

            then(userMapper).should()
                    .toDto(user, false);
        }

        @Test
        @DisplayName("동일한 권한 요청 시 권한 수정, 이벤트 발행, JWT 무효화를 수행하지 않음")
        void update_same_role() {
            // given
            UUID userId = UUID.randomUUID();

            UserRoleUpdateRequest request = new UserRoleUpdateRequest(
                    userId,
                    Role.USER
            );

            User user = mock(User.class);

            UserDto expectedResponse = new UserDto(
                    userId,
                    "user1",
                    "user1@test.com",
                    null,
                    true,
                    Role.USER
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(user.getId())
                    .willReturn(userId);

            given(user.getRole())
                    .willReturn(Role.USER);

            given(jwtRegistry.hasActiveJwtInformationByUserId(userId))
                    .willReturn(true);

            given(userMapper.toDto(user, true))
                    .willReturn(expectedResponse);

            // when
            UserDto result = authService.updateRole(request);

            // then
            assertThat(result)
                    .isEqualTo(expectedResponse);

            assertThat(result.role())
                    .isEqualTo(Role.USER);

            assertThat(result.online())
                    .isTrue();

            then(userRepository).should()
                    .findById(userId);

            then(user).should(never())
                    .updateRole(any(Role.class));

            then(jwtRegistry).should(never())
                    .invalidateJwtInformationByUserId(any(UUID.class));

            then(eventPublisher).shouldHaveNoInteractions();

            then(jwtRegistry).should()
                    .hasActiveJwtInformationByUserId(userId);

            then(userMapper).should()
                    .toDto(user, true);
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
            assertThatThrownBy(
                    () -> authService.updateRole(request)
            ).isInstanceOf(
                    UserNotFoundException.class
            );

            then(userRepository).should()
                    .findById(userId);

            then(jwtRegistry).shouldHaveNoInteractions();

            then(eventPublisher).shouldHaveNoInteractions();

            then(userMapper).should(never())
                    .toDto(
                            any(User.class),
                            anyBoolean()
                    );
        }
    }
}