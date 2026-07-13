package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public BinaryContentDto create(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "저장할 파일은 비어 있을 수 없습니다."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "unknown";
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        BinaryContent binaryContent = new BinaryContent(
                fileName,
                file.getSize(),
                contentType
        );

        BinaryContent saved =
                binaryContentRepository.save(binaryContent);

        try {
            binaryContentStorage.put(
                    saved.getId(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "바이너리 파일 저장에 실패했습니다.",
                    e
            );
        }

        return binaryContentMapper.toDto(saved);
    }

    @Override
    public BinaryContentDto find(UUID id) {
        BinaryContent binaryContent =
                binaryContentRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "BinaryContent를 찾을 수 없습니다: " + id
                                )
                        );

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public ResponseEntity<?> download(UUID id) {
        BinaryContentDto binaryContentDto = find(id);

        return binaryContentStorage.download(binaryContentDto);
    }
}