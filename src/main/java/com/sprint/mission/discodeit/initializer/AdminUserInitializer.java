package com.sprint.mission.discodeit.initializer;

import com.sprint.mission.discodeit.config.AdminUserProperties;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.AdminUserService;
import com.sprint.mission.discodeit.service.UserRoleQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements ApplicationRunner {

    private final AdminUserProperties adminUserProperties;
    private final UserRoleQueryService userRoleQueryService;
    private final AdminUserService adminUserService;

    @Override
    public void run(ApplicationArguments args) {
        // 다중 인스턴스에서의 동시 생성 제어는 이번 구현 범위에서 제외한다.
        if (userRoleQueryService.existsByRole(Role.ADMIN)) {
            log.info("관리자 계정이 이미 존재합니다.");
            return;
        }

        UserDto adminUser = adminUserService.createAdmin(createAdminUserCommand());
        log.info("관리자 계정을 생성했습니다. userId={}", adminUser.id());
    }

    private UserCreateCommand createAdminUserCommand() {
        return new UserCreateCommand(
                adminUserProperties.username(),
                adminUserProperties.password(),
                adminUserProperties.email()
        );
    }
}
