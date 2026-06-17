package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {
    BinaryContent save(BinaryContent binaryContent); //사용자 저장
    Optional<BinaryContent> findById(UUID id); //Id를 이용하여 사용자 조회
    List<BinaryContent> findAllByIdIn(List<UUID> ids); //전체 사용자 조회
    boolean existsById(UUID id); //사용자 존재 여부 확인
    void deleteById(UUID id); //사용자 삭제
}
