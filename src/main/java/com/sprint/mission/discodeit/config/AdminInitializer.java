package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "discodeit.admin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
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
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            log.debug("ADMIN 계정이 이미 존재합니다.");
            return;
        }

        User admin = new User(
                username,
                email,
                passwordEncoder.encode(password)
        );
        admin.updateRole(UserRole.ADMIN);

        User savedAdmin = userRepository.save(admin);

        UserStatus userStatus = new UserStatus(savedAdmin);
        savedAdmin.updateStatus(userStatus);
        userStatusRepository.save(userStatus);

        log.info(
                "ADMIN 계정 초기화 완료: userId={}, username={}",
                savedAdmin.getId(),
                savedAdmin.getUsername()
        );
    }
}