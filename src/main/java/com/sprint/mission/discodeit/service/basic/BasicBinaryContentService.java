package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class BasicBinaryContentService implements BinaryContentService {

  BinaryContentRepository binaryContentRepository;

  public BasicBinaryContentService(BinaryContentRepository binaryContentRepository) {
    this.binaryContentRepository = binaryContentRepository;
  }

  @Override
  public BinaryContentResponse find(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));

    return convertToResponse(binaryContent);
  }

  @Override
  public List<BinaryContentResponse> findByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAll().stream()
        .filter(b -> ids.contains(b.getId()))
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }


  private BinaryContentResponse convertToResponse(BinaryContent binaryContent) {

    return new BinaryContentResponse(
        binaryContent.getId(),
        binaryContent.getCreatedAt(),
        binaryContent.getFileName(),
        binaryContent.getSize(),
        binaryContent.getContentType(),
        binaryContent.getBytes()
    );
  }
}
