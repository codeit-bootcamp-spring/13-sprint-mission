package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JPAUserRepository extends JpaRepository<User, UUID> {
    List<User> findByEmail(String email);
    List<User> findByUsername(String name);
}
