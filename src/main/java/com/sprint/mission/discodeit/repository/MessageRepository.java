package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  // 변경 후 (channel 객체의 id로 조회/Channel_Id → channel 필드의 id를 참조)
  List<Message> findAllByChannel_Id(UUID channelId);
}
