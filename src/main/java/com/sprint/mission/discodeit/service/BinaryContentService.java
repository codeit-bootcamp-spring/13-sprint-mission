package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

  BinaryContentDto save(BinaryContentCreateRequest request);

  BinaryContentDto find(UUID contentId);

  List<BinaryContentDto> findAllByIdIn(List<UUID> contentIds);

  void delete(UUID contentId);
}
