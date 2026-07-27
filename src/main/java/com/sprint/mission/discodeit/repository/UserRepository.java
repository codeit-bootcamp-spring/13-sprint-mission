package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByEmail(String email);
    List<User> findByUsername(String name);

    @EntityGraph(attributePaths = {"profile","status"})
    @Query("SELECT a FROM User a")
    List<User> findAllWithProfile();
}
