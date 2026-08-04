package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Channel 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    // 채널 타입별 조회 및 페이징·정렬
    Page<Channel> findByType(
            ChannelType type,
            Pageable pageable
    );

    // 채널 이름에 특정 문자열이 포함된 PUBLIC 채널 조회
    Page<Channel> findByNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );
}