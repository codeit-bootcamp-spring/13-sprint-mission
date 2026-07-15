package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  // PK가 아닌 FK(channelId)를 이용해 조회 시도 - 직접 메서드 선언해야 한다
  List<Message> findByChannelId(UUID channelId);

  Page<Message> findByChannelId(UUID channelId, Pageable pageable);
}
