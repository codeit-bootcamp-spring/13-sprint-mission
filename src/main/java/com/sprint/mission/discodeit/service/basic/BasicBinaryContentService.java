package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        BinaryContent content = new BinaryContent(
                request.fileName(),
                request.size(),
                request.contentType(),
                request.bytes()
        );
        BinaryContent savedContent = binaryContentRepository.save(content);
        log.info("BinaryContent: {}가 생성됨.", savedContent.getFileName());
        return toResponse(savedContent);
    }

    @Override
    public BinaryContentResponse find(UUID binaryContentId) {
        return binaryContentRepository.findById(binaryContentId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("바이너리 콘텐츠 ID: " + binaryContentId + " 를 찾을 수 없습니다."));
    }

    @Override
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> binaryContentIds) {
        return binaryContentRepository.findAllByIdIn(binaryContentIds).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID binaryContentId) {
        if (!binaryContentRepository.existsById(binaryContentId)) {
            throw new NoSuchElementException("바이너리 콘텐츠 ID: " + binaryContentId + " 를 찾을 수 없습니다.");
        }
        binaryContentRepository.deleteById(binaryContentId);
        log.info("BinaryContent: {}가 삭제됨.", binaryContentId);
    }

    private BinaryContentResponse toResponse(BinaryContent content) {
        return new BinaryContentResponse(
                content.getId(),
                content.getFileName(),
                content.getSize(),
                content.getContentType(),
                content.getBytes(),
                content.getCreatedAt()
        );
    }
}
