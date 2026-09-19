package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByRole(Role role);

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findByUsername(String username);

  @NonNull
  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findById(@NonNull UUID id);

  @NonNull
  @EntityGraph(attributePaths = {"profile"})
  List<User> findAll();
}