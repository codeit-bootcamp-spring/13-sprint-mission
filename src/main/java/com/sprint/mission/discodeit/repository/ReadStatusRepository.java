package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

// ReadStatus 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    // 특정 User와 특정 Channel에 해당하는 ReadStatus 조회
    ReadStatus findByUserIdAndChannelId(UUID userId, UUID channelId);

    // 특정 User의 모든 ReadStatus 조회
    List<ReadStatus> findAllByUserId(UUID userId);

    // 특정 Channel의 모든 ReadStatus 조회
    List<ReadStatus> findAllByChannelId(UUID channelId);

    // 특정 User가 참여한 Channel id 목록 조회
    @Query("""
            select r.channelId
            from ReadStatus r
            where r.userId = :userId
            """)
    List<UUID> findChannelIdsByUserId(UUID userId);

    // 특정 Channel에 참여한 User id 목록 조회
    @Query("""
            select r.userId
            from ReadStatus r
            where r.channelId = :channelId
            """)
    List<UUID> findUserIdsByChannelId(UUID channelId);

    // 특정 Channel과 관련된 모든 ReadStatus 삭제
    void deleteByChannelId(UUID channelId);

    // 특정 User와 관련된 모든 ReadStatus 삭제
    void deleteByUserId(UUID userId);
}