package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// 저장로직 관련 기능 인터페이스 선언
public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
