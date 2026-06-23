package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//ReadStatus 엔티티의 데이터 접근 기능을 정의하는 Repository 인터페이스
public interface ReadStatusRepository {
    ReadStatus save(ReadStatus readStatus); //ReadStatus 저장
    Optional<ReadStatus> findById(UUID id);//ReadStatus ID로 조회
    List<ReadStatus> findAllByUserId(UUID userId); //특정 사용자의 모든 읽을 상태 조회
    List<ReadStatus> findAllByChannelId(UUID channelId); //특정 채널의 모든 읽을 상태 조회
    boolean existsById(UUID id); //특정 ReadStatus 존재 여부 확인
    void deleteById(UUID id); //ReadStatus를 이용하여 삭제
    void deleteAllByChannelId(UUID channelId); //특정 채널에 속한 모든 ReadStatus 삭제
}
