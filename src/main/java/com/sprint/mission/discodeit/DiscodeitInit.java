package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiscodeitInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // if no admin account, create admin.
        createAdminUser(userRepository.existsByRole(Role.ADMIN));
    }

    private void createAdminUser(boolean hasAdmin){
        if (hasAdmin) return;

        String email = "admin@admin.com";
        String password = "password";

        User admin = new User(
                "admin",
                email,
                passwordEncoder.encode(password),
                null,
                null
                );
        admin.updateRole(Role.ADMIN);

        userRepository.save(admin);

        log.info("init admin account made - email : {}, password : {}", email, password);
    }
}
