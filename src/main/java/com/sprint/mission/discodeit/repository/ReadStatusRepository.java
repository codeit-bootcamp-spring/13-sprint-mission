package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    // 참여자 목록 조회
    List<ReadStatus> findAllByChannelId (UUID channelId);
    // 유저가 참여한 채널 목록 조회
    List<ReadStatus> findAllByUserId (UUID userId);

    Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID ChannelId);

    @Modifying
    @Query("DELETE FROM ReadStatus rs where rs.channel.id = :channelId")
    void deleteByChannelId(@Param("channelId") UUID channelId);

    void deleteByUserId (UUID userId);

}
