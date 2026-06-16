package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {

    // 저장
    void save(ReadStatus readStatus);
    //단건조회
    Optional<ReadStatus> findById(UUID id);
    // 참여자 목록 조회
    List<ReadStatus> findAllByChannelId (UUID channelId);
    // 유저가 참여한 채널 목록 조회
    List<ReadStatus> findAllByUserId (UUID userId);

    //삭제
    void deleteById(UUID id);
    void deleteByChannelId(UUID channelId);
    void deleteByUserId (UUID userId);

}
