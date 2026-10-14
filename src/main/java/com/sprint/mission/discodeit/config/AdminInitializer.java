package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        String encryptedPassword = passwordEncoder.encode(adminProperties.password());
        User admin = new User(
                adminProperties.username(),
                adminProperties.email(),
                encryptedPassword,
                null,
                Role.ADMIN);

        userRepository.save(admin);
    }
}
