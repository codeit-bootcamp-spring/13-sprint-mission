package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        BinaryContent content = new BinaryContent(
                request.getUserId(),
                request.getMessageId(),
                request.getFilename(),
                request.getContentType(),
                request.getBytes());
        binaryContentRepository.save(content);

        return BinaryContentResponse.from(content);
    }

    public BinaryContentResponse find(UUID id) {
        BinaryContent content = binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("바이너리 컨텐츠가 없습니다."));

        return BinaryContentResponse.from(content);
    }

    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        return binaryContentRepository.findAllByIdIn(ids)
                .stream()
                .map(BinaryContentResponse::from)
                .toList();
    }

    public void delete(UUID id) {
        binaryContentRepository.delete(id);
    }

}
