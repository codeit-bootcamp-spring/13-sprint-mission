package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 저장로직 관련 기능 인터페이스 선언
public interface UserRepository {

  User save(User user);

  Optional<User> findById(UUID id);

  List<User> findAll();

  void deleteById(UUID id);

  boolean existById(UUID id);

  boolean existByEmail(String email);

  boolean existByUsername(String username);
}
