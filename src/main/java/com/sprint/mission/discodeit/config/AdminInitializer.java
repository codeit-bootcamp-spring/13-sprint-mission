package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "discodeit.admin",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AdminProperties adminProperties;

  @Transactional
  @Override
  public void run(ApplicationArguments args) throws Exception {
    boolean isAdmin = userRepository.existsByRole(Role.ADMIN);

    // 어드민 계정이 없는 경우에만 초기 관리자 계정 생성
    if (!isAdmin) {
      User admin = new User(
          adminProperties.username(),
          adminProperties.email(),
          passwordEncoder.encode(adminProperties.password()),
          null
      );
      admin.updateRole(Role.ADMIN);
      new UserStatus(admin, Instant.now());
      userRepository.save(admin);
    }
  }
}