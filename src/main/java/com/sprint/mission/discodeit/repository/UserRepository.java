package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.role.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("select u.id from User u where u.email = :email")
    Optional<UUID> findIdFromEmail(@Param("email") String email);

    @Query("select u.id from User u where u.username = :username")
    Optional<UUID> findIdFromUsername(@Param("username") String username);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"profile"})
    List<User> findAll();

    boolean existsByRole(Role role);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findByRole(Role role);
}
