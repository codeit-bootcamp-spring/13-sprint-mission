package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.basic.BasicUserRoleUpdater;
import com.sprint.mission.discodeit.service.basic.UserRoleManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 역할 변경 서비스 단위 테스트")
class UserRoleUpdaterTest {

    @InjectMocks
    BasicUserRoleUpdater userRoleUpdater;

    @Mock
    UserRoleManager userRoleManager;

    @Test
    @DisplayName("권한 검증 후 내부 역할 변경 컴포넌트에 요청을 위임한다")
    void updateRole_delegatesToUserRoleManager() {
        UUID userId = UUID.randomUUID();
        UserRoleUpdateCommand command = new UserRoleUpdateCommand(Role.CHANNEL_MANAGER);
        UserDto expected = userDto(userId, Role.CHANNEL_MANAGER);

        given(userRoleManager.updateRole(userId, command)).willReturn(expected);

        UserDto result = userRoleUpdater.updateRole(userId, command);

        assertThat(result).isSameAs(expected);
        then(userRoleManager).should().updateRole(userId, command);
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 예외를 발생시킨다")
    void updateRole_throwsException_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UserRoleUpdateCommand command = new UserRoleUpdateCommand(Role.ADMIN);
        given(userRoleManager.updateRole(userId, command)).willThrow(new UserNotFoundException(userId));

        assertThatThrownBy(() -> userRoleUpdater.updateRole(userId, command))
                .isInstanceOf(UserNotFoundException.class);

        then(userRoleManager).should().updateRole(userId, command);
    }

    private UserDto userDto(UUID userId, Role role) {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-11T10:00:00+09:00");
        return new UserDto(userId, "testUser", "test@example.com", null, false, role, now, now);
    }
}
