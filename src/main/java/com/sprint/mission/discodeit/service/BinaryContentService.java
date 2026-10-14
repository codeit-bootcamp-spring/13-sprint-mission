package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface BinaryContentService {

  BinaryContent create(MultipartFile file);

  BinaryContentDto find(UUID id);

  List<BinaryContentDto> findByIdIn(List<UUID> ids);

  ResponseEntity<?> download(UUID id);

  void updateStatus(UUID id, BinaryContent.Status status);
}
