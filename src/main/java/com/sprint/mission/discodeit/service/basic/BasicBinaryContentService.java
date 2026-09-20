package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private static final Logger log =
            LoggerFactory.getLogger(BasicBinaryContentService.class);

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public BinaryContentDto create(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("파일 업로드 실패 - 파일이 비어 있음");

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

        log.debug(
                "파일 업로드 요청: fileName={}, size={}, contentType={}",
                fileName,
                file.getSize(),
                contentType
        );

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
        } catch (IOException exception) {

            log.error(
                    "파일 업로드 실패: binaryContentId={}, fileName={}",
                    saved.getId(),
                    fileName,
                    exception
            );

            binaryContentRepository.delete(saved);

            throw new BinaryContentStorageException(
                    saved.getId(),
                    fileName,
                    exception
            );
        }

        log.info(
                "파일 업로드 완료: binaryContentId={}, fileName={}, size={}",
                saved.getId(),
                fileName,
                file.getSize()
        );

        return binaryContentMapper.toDto(saved);
    }

    @Override
    public BinaryContentDto find(UUID id) {

        log.debug("파일 메타데이터 조회 요청: binaryContentId={}", id);

        BinaryContent binaryContent =
                binaryContentRepository.findById(id)
                        .orElseThrow(() -> {

                            log.warn(
                                    "파일 메타데이터 조회 실패 - 파일 없음: binaryContentId={}",
                                    id
                            );

                            return new BinaryContentNotFoundException(id);
                        });

        log.debug("파일 메타데이터 조회 완료: binaryContentId={}", id);

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public ResponseEntity<?> download(UUID id) {

        log.debug("파일 다운로드 요청: binaryContentId={}", id);

        BinaryContentDto binaryContentDto = find(id);

        ResponseEntity<?> response =
                binaryContentStorage.download(binaryContentDto);

        log.info(
                "파일 다운로드 응답 완료: binaryContentId={}, fileName={}",
                id,
                binaryContentDto.fileName()
        );

        return response;
    }
}