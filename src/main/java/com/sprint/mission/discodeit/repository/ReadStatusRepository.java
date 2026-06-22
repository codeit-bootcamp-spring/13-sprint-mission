package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {

  ReadStatus save(ReadStatus readStatus);

  Optional<ReadStatus> findById(UUID id);

  List<ReadStatus> findAll();

  List<ReadStatus> findAllByUserId(UUID userId);

  ReadStatus update(ReadStatus readStatus);

  void delete(UUID id);

  void deleteByChannelId(UUID channelId);
}
