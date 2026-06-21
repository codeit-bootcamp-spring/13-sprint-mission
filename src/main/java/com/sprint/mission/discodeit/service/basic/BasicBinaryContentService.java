package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentResponse save(BinaryContentCreateRequest request) {
        BinaryContent binaryContent=new BinaryContent(
                request.getFileName(),
                request.getContentType(),
                request.getFileSize(),
                request.getBytes()
        );
        BinaryContent saved = binaryContentRepository.save(binaryContent);
        return BinaryContentResponse.from(saved);
    }

    @Override
    public BinaryContentResponse find(UUID contentId) {
        BinaryContent binaryContent = binaryContentRepository.findById(contentId)
                .orElseThrow(() -> new NoSuchElementException(contentId + " 를 찾을 수 없습니다."));
        return BinaryContentResponse.from(binaryContent);
    }

    @Override
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> contentIds) { // id 목록 조회
        return binaryContentRepository.findAllByIdIn(contentIds).stream()
                .map(BinaryContentResponse::from)
                .toList();
    }

    @Override
    public void delete(UUID contentId) {
        if (!binaryContentRepository.existById(contentId)){
            throw new NoSuchElementException(contentId+" 를 찾을 수 없습니다.");
        }
        binaryContentRepository.deleteById(contentId);
    }
}
