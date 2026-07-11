package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.ObjLongConsumer;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.contentType(),
                (long) request.bytes().length
        );
        binaryContentRepository.save(binaryContent);
        return toResponse(binaryContent);
    }

    @Override
    public BinaryContentResponse findById(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

        return toResponse(binaryContent);
    }
    @Override
    public Collection<BinaryContentResponse> findAllByIdIn(Collection<UUID> ids) {
        return binaryContentRepository.findAllByIdIn(ids).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        if (!binaryContentRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 파일입니다.");
        }

        binaryContentRepository.deleteById(id);
    }

    @Override
    public BinaryContent findEntityById(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));
    }

    private BinaryContentResponse toResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getSize()
        );
    }
}
