package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.basic.BasicAdminUserService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 사용자 서비스 단위 테스트")
class AdminUserServiceTest {

    @InjectMocks
    BasicAdminUserService adminUserService;

    @Mock
    UserService userService;

    @Mock
    UserRoleManager userRoleManager;

    @Test
    @DisplayName("사용자를 생성한 뒤 관리자 역할로 변경한다")
    void createAdmin_createsUserAndUpdatesRole() {
        UserCreateCommand command = new UserCreateCommand(
                "admin",
                "adminPassword",
                "admin@example.com"
        );
        UUID userId = UUID.randomUUID();
        UserDto createdUser = userDto(userId, Role.USER);
        UserDto expected = userDto(userId, Role.ADMIN);
        UserRoleUpdateCommand roleCommand = new UserRoleUpdateCommand(Role.ADMIN);

        given(userService.create(command, null)).willReturn(createdUser);
        given(userRoleManager.updateRole(userId, roleCommand)).willReturn(expected);

        UserDto result = adminUserService.createAdmin(command);

        assertThat(result).isSameAs(expected);
        then(userService).should().create(command, null);
        then(userRoleManager).should().updateRole(userId, roleCommand);
    }

    private UserDto userDto(UUID userId, Role role) {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-11T10:00:00+09:00");
        return new UserDto(userId, "admin", "admin@example.com", null, false, role, now, now);
    }
}
