package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {

    Optional<UserStatus> findByUserId(UUID userId);

    Collection<UserStatus> findAllByUser_IdIn(Collection<UUID> userIds);
}