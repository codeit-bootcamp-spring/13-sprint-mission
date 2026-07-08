package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

  BinaryContentDto create(String fileName, long size, String contentType);

  BinaryContentDto find(UUID binaryContentId);

  List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds);

  void delete(UUID binaryContentId);
}
