package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        if (userRepository.existsByUsername(adminProperties.username())
                || userRepository.existsByEmail(adminProperties.email())) {
            throw new IllegalStateException(
                    "관리자 username 또는 email이 기존 사용자와 중복됩니다."
            );
        }

        User admin = new User(
                adminProperties.username(),
                adminProperties.email(),
                passwordEncoder.encode(adminProperties.password())
        );

        admin.updateRole(Role.ADMIN);
        userRepository.save(admin);
    }
}