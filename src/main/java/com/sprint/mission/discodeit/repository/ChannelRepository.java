package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

  @Query("""
      select channel
      from Channel channel
      where channel.type = :publicType
         or exists (
           select 1
           from ReadStatus readStatus
           where readStatus.channel = channel
             and readStatus.user.id = :userId
         )
      """)
  List<Channel> findVisibleChannelsByUserId(
      @Param("userId") UUID userId,
      @Param("publicType") ChannelType publicType
  );

}
