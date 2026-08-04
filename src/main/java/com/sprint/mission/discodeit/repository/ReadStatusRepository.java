package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
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
}
