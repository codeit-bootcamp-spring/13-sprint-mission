package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JAPReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    List<ReadStatus> findByChannelId(UUID id);
    List<ReadStatus> findByUserId(UUID id);

    UUID user(User user);
}
