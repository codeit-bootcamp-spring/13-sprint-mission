package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class BasicBinaryContentService implements BinaryContentService {

  BinaryContentRepository binaryContentRepository;

  public BasicBinaryContentService(BinaryContentRepository binaryContentRepository) {
    this.binaryContentRepository = binaryContentRepository;
  }

  @Override
  public BinaryContentDto find(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));

    return convertToResponse(binaryContent);
  }

  @Override
  public List<BinaryContentDto> findByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAll().stream()
        .filter(b -> ids.contains(b.getId()))
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }


  private BinaryContentDto convertToResponse(BinaryContent binaryContent) {

    return new BinaryContentDto(
        binaryContent.getId(),
        binaryContent.getFileName(),
        binaryContent.getSize(),
        binaryContent.getContentType()
    );
  }
}
