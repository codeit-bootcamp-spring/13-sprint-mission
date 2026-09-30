package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    List<ReadStatus> findByChannelId(UUID id);
    List<ReadStatus> findByUserId(UUID id);
    List<ReadStatus> findByChannelType(ChannelType type);

    // fetch join for get channel, user info
    @Query("SELECT rs FROM ReadStatus rs JOIN FETCH rs.user u JOIN FETCH rs.channel c WHERE u.id = :id")
    List<ReadStatus> findWithDetailByUserId(@Param("id") UUID id);

    @Query("SELECT rs FROM ReadStatus rs JOIN FETCH rs.user u JOIN FETCH rs.channel c WHERE c.type = :type")
    List<ReadStatus> findWithDetailByChannelType(@Param("type") ChannelType type);

    @Query("select rs.channel.id from ReadStatus rs where rs.user.id = :id")
    List<UUID> findChannelIdByUserId(@Param("id") UUID userId);

    @Query("select rs.user.id from ReadStatus rs where rs.channel.id = :id")
    List<UUID> findUserIdsByChannelId(@Param("id") UUID channelId);

    @Query("select rs from ReadStatus rs where rs.channel.type = ChannelType.PUBLIC")
    List<ReadStatus> findAllByUserVisible(@Param("id") UUID userId);

    List<ReadStatus> findAllByUserId(UUID userId);

    boolean existsByUserOrChannel(User user, Channel channel);
}
