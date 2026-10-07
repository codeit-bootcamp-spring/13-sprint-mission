package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.querydsl.UserQueryDsl;
import com.sprint.mission.discodeit.security.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, UserQueryDsl {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("select u.id from User u where u.email = :email")
    Optional<UUID> findIdFromEmail(@Param("email") String email);

    boolean existsByRole(Role role);
    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
