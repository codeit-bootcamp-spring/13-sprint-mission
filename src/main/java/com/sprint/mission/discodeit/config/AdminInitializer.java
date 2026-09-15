package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${discodeit.admin.username}")
    private String username;
    @Value("${discodeit.admin.email}")
    private String email;
    @Value("${discodeit.admin.password}")
    private String password;


    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        User admin = new User(username, email, passwordEncoder.encode(password));

        admin.updateRole(Role.ADMIN);
        userRepository.save(admin);

        UserStatus userStatus = new UserStatus(admin);
        userStatusRepository.save(userStatus);

    }
}
