package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        // ADMIN 계정이 없을때 만 생성
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User(
                    "admin",
                    "admin@discodeit.com",
                    passwordEncoder.encode("admin1234"),
                    Role.ADMIN,
                    null,
                    null
            );
            userRepository.save(admin);
        }
    }
}
