package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;



public interface JPAUserStatusRepository extends JpaRepository<UserStatus, UUID> {
    List<UserStatus> findByUserId(UUID userID);
}
