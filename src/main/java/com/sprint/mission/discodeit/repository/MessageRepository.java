package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findByChannel_Id(UUID channelId);

  Slice<Message> findByChannel_Id(
      UUID channelId,
      Pageable pageable
  );

  Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(
      UUID channelId
  );

  @Query("""
      select message.channel.id, max(message.createdAt)
      from Message message
      where message.channel.id in :channelIds
      group by message.channel.id
      """)
  List<Object[]> findLastMessageAtByChannelIds(
      @Param("channelIds") List<UUID> channelIds
  );

  void deleteByChannel_Id(UUID channelId);
}