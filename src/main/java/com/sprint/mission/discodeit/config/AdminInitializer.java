package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserData;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("ADMIN 계정이 이미 존재합니다.");
            return;
        }

        UserData userData = new UserData(
                "admin",
                "admin@discodeit.com",
                passwordEncoder.encode("admin1234")
        );

        User admin = new User(userData);

        admin.updateRole(Role.ADMIN);

        User savedAdmin = userRepository.save(admin);

        log.info(
                "ADMIN 계정이 초기화되었습니다. username={}",
                savedAdmin.getUsername()
        );
    }
}