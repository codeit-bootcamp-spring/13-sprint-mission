package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class BasicBinaryContentService implements BinaryContentService {

    BinaryContentRepository binaryContentRepository;

    public BasicBinaryContentService(BinaryContentRepository binaryContentRepository) {
        this.binaryContentRepository = binaryContentRepository;
    }


    @Override
    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = BinaryContent.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .fileName(request.fileName())
                .fileUrl(request.fileUrl())
                .fileSize(request.fileSize())
                .build();

        binaryContentRepository.save(binaryContent);

        return convertToResponse(binaryContent);
    }

    @Override
    public BinaryContentResponse find(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일 콘텐츠입니다."));

        return convertToResponse(binaryContent);
    }

    @Override
    public List<BinaryContentResponse> findByUserIdIn(List<UUID> ids) {
        return binaryContentRepository.findAll().stream()
                .filter(b -> ids.contains(b.getId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        BinaryContent binaryContent =binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일 콘텐츠입니다."));

        binaryContentRepository.delete(id);

    }

    private BinaryContentResponse convertToResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getFileUrl(),
                binaryContent.getFileSize(),
                binaryContent.getMessageId()
        );
    }

}
