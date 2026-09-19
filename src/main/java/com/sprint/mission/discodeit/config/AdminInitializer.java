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
  
  private static final String ADMIN_EMAIL = "admin@discodeit.com";
  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_PASSWORD = "admin1234";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (userRepository.existsByRole(Role.ADMIN)) {
      return;
    }

    User admin = new User(
        ADMIN_EMAIL,
        ADMIN_USERNAME,
        passwordEncoder.encode(ADMIN_PASSWORD)
    );

    admin.updateRole(Role.ADMIN);

    userRepository.save(admin);
  }
}