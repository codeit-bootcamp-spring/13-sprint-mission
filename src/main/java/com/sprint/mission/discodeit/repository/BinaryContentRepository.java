package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//BinaryContent 엔티티의 데이터 접근 기능을 정의하는 Repository 인터페이스
public interface BinaryContentRepository {
    BinaryContent save(BinaryContent binaryContent); //BinaryContent 저장
    Optional<BinaryContent> findById(UUID id); //BinaryContent Id를 이용하여 조회
    List<BinaryContent> findAllByIdIn(List<UUID> ids); //여러 개의 BinaryContent 조회
    boolean existsById(UUID id); //특정 BinaryContent 존재 여부 확인
    void deleteById(UUID id); //BinaryContent 삭제
}
