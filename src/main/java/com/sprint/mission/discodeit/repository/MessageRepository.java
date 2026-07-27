package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  // PK가 아닌 FK(channelId)를 이용해 조회 시도 - 직접 메서드 선언해야 한다
  List<Message> findByChannelId(UUID channelId);

  Slice<Message> findAllByChannelId(UUID channelId, Pageable pageable);
  // 총 메시지가 몇 개인지 알 필요 없기 때문에 Slice<Message> 타입으로 반환하도록 변경
}
