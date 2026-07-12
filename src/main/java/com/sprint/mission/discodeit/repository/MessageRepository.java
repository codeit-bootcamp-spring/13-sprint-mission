package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findAllByChannel_Id(UUID channelId);

  // 페이지네이션 추가 — 채널 ID로 메시지를 페이지 단위로 조회
  Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable);

  void deleteAllByChannel_Id(UUID channelId);
}