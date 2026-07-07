package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    // 참여자 목록 조회
    List<ReadStatus> findAllByChannelId (UUID channelId);
    // 유저가 참여한 채널 목록 조회
    List<ReadStatus> findAllByUserId (UUID userId);

    Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID ChannelId);

    //삭제
    void deleteByChannelId(UUID channelId);
    void deleteByUserId (UUID userId);

}
