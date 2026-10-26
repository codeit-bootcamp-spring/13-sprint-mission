package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SseService sseService;

    @InjectMocks
    private BasicAuthService authService;

    @Test
    void updateRole_사용자_권한을_변경하고_이벤트를_발행한다() {
        UUID userId = UUID.randomUUID();

        UserRoleUpdateRequest request =
                new UserRoleUpdateRequest(
                        userId,
                        UserRole.CHANNEL_MANAGER
                );

        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        ReflectionTestUtils.setField(user, "id", userId);

        UserResponse expected = new UserResponse(
                userId,
                "codeit",
                "codeit@example.com",
                false,
                null,
                UserRole.CHANNEL_MANAGER
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(userMapper.toDto(user))
                .willReturn(expected);

        UserResponse actual = authService.updateRole(request);

        assertThat(actual).isEqualTo(expected);
        assertThat(user.getRole())
                .isEqualTo(UserRole.CHANNEL_MANAGER);

        ArgumentCaptor<RoleUpdatedEvent> eventCaptor =
                ArgumentCaptor.forClass(RoleUpdatedEvent.class);

        then(eventPublisher)
                .should()
                .publishEvent(eventCaptor.capture());

        RoleUpdatedEvent event = eventCaptor.getValue();

        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.previousRole())
                .isEqualTo(UserRole.USER);
        assertThat(event.updatedRole())
                .isEqualTo(UserRole.CHANNEL_MANAGER);

        then(userMapper).should().toDto(user);
        then(sseService).should()
                .broadcast("users.updated", expected);
    }

    @Test
    void updateRole_같은_권한이면_권한_이벤트는_발행하지_않는다() {
        UUID userId = UUID.randomUUID();

        UserRoleUpdateRequest request =
                new UserRoleUpdateRequest(
                        userId,
                        UserRole.USER
                );

        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        ReflectionTestUtils.setField(user, "id", userId);

        UserResponse expected = new UserResponse(
                userId,
                "codeit",
                "codeit@example.com",
                false,
                null,
                UserRole.USER
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(userMapper.toDto(user))
                .willReturn(expected);

        UserResponse actual = authService.updateRole(request);

        assertThat(actual).isEqualTo(expected);

        then(eventPublisher).shouldHaveNoInteractions();
        then(sseService).should()
                .broadcast("users.updated", expected);
    }

    @Test
    void updateRole_사용자가_없으면_실패한다() {
        UUID userId = UUID.randomUUID();

        UserRoleUpdateRequest request =
                new UserRoleUpdateRequest(
                        userId,
                        UserRole.ADMIN
                );

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> authService.updateRole(request)
        )
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        then(userMapper).shouldHaveNoInteractions();
        then(eventPublisher).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }
}