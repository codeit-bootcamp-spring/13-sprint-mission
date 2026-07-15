package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 생성 요청은 비어 있을 수 없습니다.");
        }

        validate(request);

        BinaryContent binaryContent = new BinaryContent(
                request.getFileName(),
                safeContentType(request.getContentType()),
                (long) request.getBytes().length
        );

        BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

        binaryContentStorage.put(
                savedBinaryContent.getId(),
                request.getBytes()
        );

        return toResponse(savedBinaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentResponse find(UUID id) {
        BinaryContent binaryContent = findBinaryContentById(id);

        return toResponse(binaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentDownloadResponse findForDownload(UUID id) {
        BinaryContent binaryContent = findBinaryContentById(id);

        byte[] bytes = binaryContentStorage.get(binaryContent.getId());

        return toDownloadResponse(binaryContent, bytes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        List<BinaryContentResponse> responses = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            return responses;
        }

        List<BinaryContent> binaryContents = binaryContentRepository.findAllByIdIn(ids);

        for (BinaryContent binaryContent : binaryContents) {
            responses.add(toResponse(binaryContent));
        }

        return responses;
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("삭제할 바이너리 콘텐츠 id는 null일 수 없습니다.");
        }

        if (!binaryContentRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 바이너리 콘텐츠를 찾을 수 없습니다. id=" + id);
        }

        binaryContentRepository.deleteById(id);
        binaryContentStorage.delete(id);
    }

    private BinaryContent findBinaryContentById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("조회할 바이너리 콘텐츠 id는 null일 수 없습니다.");
        }

        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "조회할 바이너리 콘텐츠를 찾을 수 없습니다. id=" + id
                ));
    }

    private void validate(BinaryContentCreateRequest request) {
        if (request.getFileName() == null || request.getFileName().isBlank()) {
            throw new IllegalArgumentException("파일 이름은 비어 있을 수 없습니다.");
        }

        if (request.getBytes() == null || request.getBytes().length == 0) {
            throw new IllegalArgumentException("파일 데이터는 비어 있을 수 없습니다.");
        }
    }

    private BinaryContentResponse toResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getUpdatedAt(),
                binaryContent.getFileName(),
                safeContentType(binaryContent.getContentType()),
                binaryContent.getSize()
        );
    }

    private BinaryContentDownloadResponse toDownloadResponse(
            BinaryContent binaryContent,
            byte[] bytes
    ) {
        return new BinaryContentDownloadResponse(
                binaryContent.getId(),
                binaryContent.getFileName(),
                safeContentType(binaryContent.getContentType()),
                bytes,
                binaryContent.getSize()
        );
    }

    private String safeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }
}