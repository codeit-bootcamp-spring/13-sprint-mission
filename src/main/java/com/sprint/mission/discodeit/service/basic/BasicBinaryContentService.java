package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContent.Status;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentUploadException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Override
  public BinaryContent create(MultipartFile file) {
    byte[] bytes;

    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      throw new BinaryContentUploadException(file.getOriginalFilename(), e);
    }

    BinaryContent binaryContent = new BinaryContent(
        file.getOriginalFilename(),
        file.getSize(),
        file.getContentType()
    );

    binaryContentRepository.save(binaryContent);

    eventPublisher.publishEvent(new BinaryContentCreatedEvent(binaryContent.getId(), bytes));

    return binaryContent;
  }

  @Transactional(readOnly = true)
  @Override
  public BinaryContentDto find(UUID id) {
    log.debug("파일 정보 조회 시작: binaryContentId={}", id);

    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> {
          log.warn(
              "파일 정보 조회 실패: binaryContentId={}를 찾을 수 없음",
              id
          );
          return new BinaryContentNotFoundException(id);
        });

    log.debug("파일 정보 조회 완료: binaryContentId={}", id);

    return binaryContentMapper.toDto(binaryContent);
  }

  @Transactional(readOnly = true)
  @Override
  public List<BinaryContentDto> findByIdIn(List<UUID> ids) {
    log.debug("파일 정보 목록 조회 시작: requestedCount={}", ids.size());

    List<BinaryContentDto> binaryContents =
        binaryContentRepository.findAllById(ids).stream()
            .map(binaryContentMapper::toDto)
            .toList();

    log.debug(
        "파일 정보 목록 조회 완료: requestedCount={}, resultCount={}",
        ids.size(),
        binaryContents.size()
    );

    return binaryContents;
  }

  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<?> download(UUID id) {
    log.debug("파일 다운로드 처리 시작: binaryContentId={}", id);

    BinaryContentDto binaryContentDto = find(id);

    ResponseEntity<?> response = binaryContentStorage.download(binaryContentDto);

    log.info("파일 다운로드 처리 완료: binaryContentId={}, status={}", id, response.getStatusCode());

    return response;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void updateStatus(UUID id, Status status) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));

    binaryContent.updateStatus(status);
  }
}
