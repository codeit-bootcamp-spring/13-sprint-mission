package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  // 변경 후 (channel 객체의 id로 조회/Channel_Id → channel 필드의 id를 참조)
  @EntityGraph(attributePaths = {"author", "author.userStatus", "author.profile", "channel"})
  Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable);
  //파라미터의 channelId에 속한 모든 message객체를 반환

  Optional<Message> findFirstByChannel_IdOrderByCreatedAtDesc(UUID channelId);
  //파라미터로 받은 channelId에 속하는 message 객체들 중에
  // createdAt 기준 가장 최신의 message 객체를 Optional로 감싸서 반환 한다.


  //cursor가 있으면 cursor이전 메세지 조회
  @EntityGraph(attributePaths = {"author", "author.userStatus", "author.profile", "channel"})
  Slice<Message> findAllByChannel_IdAndCreatedAtBefore(UUID channelId, Instant cursor,
      Pageable pageable);


}