package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper; // 매퍼 객체 사용하기 위해 의존성 주입
  private final BinaryContentStorage storage;

  @Transactional // readOnly가 아닌 트랜잭션이 필요한 경우 메서드에 직접 붙여준다
  @Override
  public BinaryContentDto save(BinaryContentCreateRequest request) {
    BinaryContent binaryContent = new BinaryContent(
        request.getFileName(),
        request.getContentType(),
        (long) request.getBytes().length
    );
    BinaryContent saved = binaryContentRepository.save(binaryContent);
    storage.put(saved.getId(), request.getBytes());
    log.info("첨부 파일 생성 id={}, fileName={}", saved.getId(), request.getFileName());
    return binaryContentMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public BinaryContentDto find(UUID binaryContentId) {
    log.debug("첨부 파일 조회 binaryContentId={}", binaryContentId);
    return binaryContentRepository.findById(binaryContentId)
        .map(binaryContent -> binaryContentMapper.toDto(binaryContent))
        .orElseThrow(
            () -> new BinaryContentNotFoundException(binaryContentId));
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> contentIds) { // id 목록 조회
    if (contentIds == null || contentIds.isEmpty()) {
      return List.of();
    }
    return binaryContentRepository.findAllById(contentIds).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public void delete(UUID contentId) {
    if (!binaryContentRepository.existsById(contentId)) {
      log.warn("존재하지 않는 파일 아이디 {}", contentId);
      throw new BinaryContentNotFoundException(contentId);
    }
    log.info("첨부 파일 삭제 contentId={}", contentId);
    binaryContentRepository.deleteById(contentId);
  }
}
