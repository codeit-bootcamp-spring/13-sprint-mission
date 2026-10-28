package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TransactionTemplate transactions;
  private final String adminUsername;
  private final String adminEmail;
  private final String adminPassword;

  public AdminInitializer(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      PlatformTransactionManager transactionManager,
      @Value("${discodeit.admin.username}") String adminUsername,
      @Value("${discodeit.admin.email}") String adminEmail,
      @Value("${discodeit.admin.password}") String adminPassword
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.transactions = new TransactionTemplate(transactionManager);
    this.adminUsername = adminUsername;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      transactions.executeWithoutResult(status -> createAdminIfAbsent());
    } catch (DataIntegrityViolationException exception) {
      // 다른 서버가 먼저 생성했는지는 실패한 트랜잭션의 롤백 후 확인한다.
      if (!userRepository.existsByUsername(adminUsername)
          && !userRepository.existsByEmail(adminEmail)) {
        throw exception;
      }
    }
  }

  private void createAdminIfAbsent() {
    if (userRepository.existsByUsername(adminUsername)
        || userRepository.existsByEmail(adminEmail)) {
      return;
    }

    User admin = User.builder()
        .username(adminUsername)
        .email(adminEmail)
        .password(passwordEncoder.encode(adminPassword))
        .role(Role.ADMIN)
        .build();
    userRepository.save(admin);
  }
}
