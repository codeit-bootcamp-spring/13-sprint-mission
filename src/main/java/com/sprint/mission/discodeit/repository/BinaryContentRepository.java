package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {
  // JpaRepository를 상속 받으면 구현체가 알아서 세팅된다
  // save, findById, findAll, deletedById, existById, count 가 따라온다
}
