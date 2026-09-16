package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${discodeit.admin.email}")
  private String adminEmail;

  @Value("${discodeit.admin.password}")
  private String adminPassword;

  @Override
  public void run(String... args) throws Exception {

    boolean userByRole = userRepository.existsByRole(Role.ADMIN);

    if (!userByRole) {
      String encodePw = passwordEncoder.encode(adminPassword);
      User admin = new User("운영자", encodePw, adminEmail, Role.ADMIN);
      userRepository.save(admin);
      System.out.println("admin 생성 완료 adminId: " + admin.getId());
    }

  }
}
