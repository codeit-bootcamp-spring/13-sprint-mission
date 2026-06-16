package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {
    Optional<BinaryContent> findById(UUID id); // 단일 조회
    BinaryContent save(BinaryContent content); // 바이너리 데이터 저장
    void deleteById(UUID id); // 바이너리 데이터 삭제
    boolean existById(UUID id);
    List<BinaryContent> findAllByIdIn(List<UUID> ids);
}
