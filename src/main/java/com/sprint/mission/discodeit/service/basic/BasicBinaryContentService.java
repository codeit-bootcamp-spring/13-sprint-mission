package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.ContentType;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    private BinaryContentResponse toResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getSize(),
                ContentType.valueOf(binaryContent.getContentType()),
                binaryContent.getBytes()
        );
    }


    @Override
    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.fileSize(),
                request.contentType(),
                request.bytes()
        );
        binaryContentRepository.save(binaryContent);
        return toResponse(binaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentResponse find(UUID id) {
        BinaryContent findByContent = binaryContentRepository
                .findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 content 입니다."));
        return toResponse(findByContent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        return ids.stream()
                .map(id ->
                        toResponse(binaryContentRepository.findById(id)
                                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 content 입니다."))))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 content 입니다."));
        binaryContentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContent findEntity(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 content 입니다."));
    }
}

