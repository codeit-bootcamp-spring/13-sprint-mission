package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;

  @Override
  @Transactional
  public BinaryContentDto create(String fileName, long size, String contentType) {
    log.info("BinaryContent 생성 요청 - fileName: {}, size: {}", fileName, size);

    BinaryContent binaryContent = new BinaryContent(fileName, size, contentType);
    BinaryContent saved = binaryContentRepository.save(binaryContent);

    log.info("BinaryContent 생성 완료 - id: {}", saved.getId());
    return binaryContentMapper.toDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public BinaryContentDto find(UUID binaryContentId) {
    log.info("BinaryContent 단건 조회 요천 - id: {}", binaryContentId);

    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new NoSuchElementException(
            "BinaryContent with id " + binaryContentId + " not found"));

    log.info("BinaryContent 단건 조회 완료 - id: {}", binaryContentId);
    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds) {
    log.info("BinaryContent 다건 조회 - ids: {}", binaryContentIds);

    List<BinaryContent> allByIdIn = binaryContentRepository.findAllByIdIn(binaryContentIds);

    log.info("BinaryContent 다건 조회 완료 - 조회된 수: {}", allByIdIn.size());
    return allByIdIn.stream()
        .map(binaryContentMapper::toDto).toList();
  }

  @Override
  @Transactional
  public void delete(UUID binaryContentId) {
    log.info("BinaryContent 삭제 요청 - id: {}", binaryContentId);

    if (!binaryContentRepository.existsById(binaryContentId)) {
      throw new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found");
    }
    binaryContentRepository.deleteById(binaryContentId);

    log.info("BinaryContent 삭제 완료 - id: {}", binaryContentId);
  }
}