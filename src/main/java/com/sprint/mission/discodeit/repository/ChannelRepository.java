package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Channel 데이터를 저장하고 조회하기 위한 Spring Data JPA Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {
}