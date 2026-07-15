package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// BinaryContent 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {

    // 여러 BinaryContent id를 한 번에 조회
    // Message 첨부파일 id 목록을 실제 BinaryContent 목록으로 조회할 때 사용
    List<BinaryContent> findAllByIdIn(List<UUID> ids);
}