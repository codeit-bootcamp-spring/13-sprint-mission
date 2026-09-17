package com.sprint.mission.discodeit.initializer;

import com.sprint.mission.discodeit.config.AdminUserProperties;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.AdminUserService;
import com.sprint.mission.discodeit.service.UserRoleQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 사용자 초기화 단위 테스트")
class AdminUserInitializerTest {

    @Mock
    UserRoleQueryService userRoleQueryService;

    @Mock
    AdminUserService adminUserService;

    @Mock
    ApplicationArguments applicationArguments;

    AdminUserProperties adminUserProperties;
    AdminUserInitializer adminUserInitializer;

    @BeforeEach
    void setUp() {
        adminUserProperties = new AdminUserProperties(
                "admin",
                "adminPassword",
                "admin@example.com"
        );
        adminUserInitializer = new AdminUserInitializer(
                adminUserProperties,
                userRoleQueryService,
                adminUserService
        );
    }

    @Test
    @DisplayName("관리자가 이미 존재하면 새 관리자를 생성하지 않는다")
    void run_doesNotCreateAdmin_whenAdminExists() {
        given(userRoleQueryService.existsByRole(Role.ADMIN)).willReturn(true);

        adminUserInitializer.run(applicationArguments);

        then(userRoleQueryService).should().existsByRole(Role.ADMIN);
        then(adminUserService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("관리자가 존재하지 않으면 설정값으로 관리자를 생성한다")
    void run_createsAdmin_whenAdminDoesNotExist() {
        UserCreateCommand expectedCommand = new UserCreateCommand(
                adminUserProperties.username(),
                adminUserProperties.password(),
                adminUserProperties.email()
        );
        given(userRoleQueryService.existsByRole(Role.ADMIN)).willReturn(false);
        given(adminUserService.createAdmin(expectedCommand)).willReturn(adminUserDto());

        adminUserInitializer.run(applicationArguments);

        then(userRoleQueryService).should().existsByRole(Role.ADMIN);
        then(adminUserService).should().createAdmin(expectedCommand);
    }

    private UserDto adminUserDto() {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-11T10:00:00+09:00");
        return new UserDto(
                UUID.randomUUID(),
                adminUserProperties.username(),
                adminUserProperties.email(),
                null,
                false,
                Role.ADMIN,
                now,
                now
        );
    }
}
