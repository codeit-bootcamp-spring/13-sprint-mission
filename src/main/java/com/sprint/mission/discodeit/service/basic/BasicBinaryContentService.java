package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true) // 클래스 레벨에 트랜잭션 설정해 모든 메서드가 readOnly 트랜잭션을 가지도록 한다
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper; // 매퍼 객체 사용하기 위해 의존성 주입 

  @Transactional // readOnly가 아닌 트랜잭션이 필요한 경우 메서드에 직접 붙여준다
  @Override
  public BinaryContentDto save(BinaryContentCreateRequest request) {
    BinaryContent binaryContent = new BinaryContent(
        request.getFileName(),
        request.getContentType(),
        (long) request.getBytes().length,
        request.getBytes()
    );
    BinaryContent saved = binaryContentRepository.save(binaryContent);
    return binaryContentMapper.toDto(saved);
  }

  @Override
  public BinaryContentDto find(UUID binaryContentId) {
    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
        .orElseThrow(
            () -> new NoSuchElementException(
                "BinaryContent with id " + binaryContentId + " not found"));
    return binaryContentMapper.toDto(binaryContent);
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
      throw new NoSuchElementException("BinaryContent with id " + contentId + " not found");
    }
    binaryContentRepository.deleteById(contentId);
  }
}
