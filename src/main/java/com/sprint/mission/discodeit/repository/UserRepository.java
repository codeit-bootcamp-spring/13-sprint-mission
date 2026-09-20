package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.querydsl.UserQueryDsl;
import com.sprint.mission.discodeit.security.role.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, UserQueryDsl {
    List<User> findByEmail(String email);
    List<User> findByUsername(String name);

    @EntityGraph(attributePaths = {"profile"})
    @Query("SELECT a FROM User a")
    List<User> findAllWithProfile();

    UUID id(UUID id);

    boolean existsByRole(Role role);
}
