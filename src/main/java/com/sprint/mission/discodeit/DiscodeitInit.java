package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserDuplicatedException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiscodeitInit implements CommandLineRunner {

    private final UserService userService;
    private final AuthService authService;

    @Override
    public void run(String... args) {

        // if no admin account, create admin.
        createAdminUser();
    }

    private void createAdminUser(){
        try{

            String defaultPassword = "password";

            // 초기 관리자 유저 생성.
            UserDto admin = userService.create(
                    new UserCreateRequest(
                            "admin",
                            "admin@admin.com",
                            defaultPassword
                    ),
                    Optional.empty()
            );
            // 권한 admin 으로 설정.
            authService.roleUpdate(admin.id(), Role.ADMIN);

            log.info("[Init] 초기 관리자 계정 생성 - ID:{}(NAME:{}), PW:{}",
                    admin.email(), admin.username(), defaultPassword);

        } catch (UserDuplicatedException e) {
            log.debug("[Init] 관리자 계정 존재. 계정 생성 스킵");
        } catch (RuntimeException e) {
            log.error("[Init] 초기 관리자 권한 생성 오류.",e);
        }
    }
}
