package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import java.io.IOException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BinaryContentService {

  private final BinaryContentRepository repository;
  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentMapper binaryContentMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public BinaryContentResponse create(BinaryContentCreateRequest request) {
    MultipartFile file = request.file();
    try {
      byte[] bytes = file.getBytes();
      BinaryContent binaryContent = repository.save(new BinaryContent(
          file.getOriginalFilename(), file.getSize(), file.getContentType(), null, null));
      eventPublisher.publishEvent(new BinaryContentCreatedEvent(binaryContent.getId(), bytes));
      return binaryContentMapper.toDto(binaryContent);
    } catch (IOException e) {
      throw new BinaryContentStorageException(file.getOriginalFilename(), e);
    }
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateStatus(UUID id, BinaryContentStatus status) {
    BinaryContent binaryContent = repository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));
    binaryContent.updateStatus(status);
  }

  public BinaryContentResponse findBinaryContentById(UUID id) {
    BinaryContent binaryContent = repository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));

    return binaryContentMapper.toDto(binaryContent);
  }

  public List<BinaryContentResponse> findAllBinaryContent(List<UUID> idList) {
    return idList.stream()
        .map(id -> repository.findById(id)
            .orElseThrow(() -> new BinaryContentNotFoundException(id)))
        .map(binaryContentMapper::toDto)
        .toList();
  }

  public ResponseEntity<Resource> downloadBinaryContent(UUID id) {
    log.info("파일 다운로드 시작: binaryContentId={}", id);
    BinaryContent binaryContent = repository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));

    ResponseEntity<Resource> response =
        binaryContentStorage.download(binaryContentMapper.toDto(binaryContent));
    log.info("파일 다운로드 완료: binaryContentId={}, size={}, contentType={}",
        id, binaryContent.getSize(), binaryContent.getContentType());
    return response;
  }

}
