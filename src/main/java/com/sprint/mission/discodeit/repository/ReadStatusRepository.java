package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  // PK 가 아닌 FK(userId channelId)를 이용해 조회를 시도할 때는 직접 메서드를 선언해야 한다
  List<ReadStatus> findByChannelId(UUID channelId);

  List<ReadStatus> findByUserId(UUID userId);

  boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);
}
