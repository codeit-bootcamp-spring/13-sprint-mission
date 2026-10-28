package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.FileUploadDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.exception.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public BinaryContentDto create(FileUploadDto file) {
    BinaryContent content = new BinaryContent(
        file.fileName(),
        "/api/binaryContents/",
        file.size(),
        file.contentType()
    );

    binaryContentRepository.save(content);

    eventPublisher.publishEvent(
        new BinaryContentCreatedEvent(content.getId(), file.bytes())
    );

    return binaryContentMapper.toDto(content);
  }

  @Override
  public BinaryContentDto find(UUID id) {
    BinaryContent content = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(Map.of("binaryContentId", id)));
    return binaryContentMapper.toDto(content);
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    BinaryContent content = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(Map.of("binaryContentId", id)));
    binaryContentRepository.delete(content);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BinaryContentDto updateStatus(UUID id, BinaryContentStatus status) {
    BinaryContent content = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(
            Map.of("binaryContentId", id)
        ));

    content.updateStatus(status);

    BinaryContentDto binaryContentDto = binaryContentMapper.toDto(content);

    eventPublisher.publishEvent(
        new BinaryContentUpdatedEvent(binaryContentDto)
    );

    return binaryContentDto;
  }
}