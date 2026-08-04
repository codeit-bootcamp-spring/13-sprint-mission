package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    public BinaryContentResponse create(BinaryContentCreateRequest request) {
        log.info(
                "파일 업로드를 시작합니다. fileName={}",
                request == null ? null : request.getFileName()
        );

        if (request == null) {
            log.warn("파일 업로드 요청이 비어 있습니다.");
            throw new IllegalArgumentException(
                    "바이너리 콘텐츠 생성 요청은 비어 있을 수 없습니다."
            );
        }

        validate(request);

        String contentType = safeContentType(request.getContentType());
        long size = request.getBytes().length;

        log.debug(
                "파일 업로드 요청을 처리합니다. fileName={}, contentType={}, size={}",
                request.getFileName(),
                contentType,
                size
        );

        BinaryContent savedBinaryContent = null;

        try {
            BinaryContent binaryContent = new BinaryContent(
                    request.getFileName(),
                    contentType,
                    size
            );

            // 파일 메타데이터를 DB에 먼저 저장합니다.
            savedBinaryContent =
                    binaryContentRepository.save(binaryContent);

            log.debug(
                    "파일 메타데이터를 저장했습니다. binaryContentId={}, fileName={}",
                    savedBinaryContent.getId(),
                    savedBinaryContent.getFileName()
            );

            // 실제 파일 데이터는 로컬 또는 외부 저장소에 저장합니다.
            binaryContentStorage.put(
                    savedBinaryContent.getId(),
                    request.getBytes()
            );

            log.info(
                    "파일 업로드가 완료되었습니다. binaryContentId={}, fileName={}, contentType={}, size={}",
                    savedBinaryContent.getId(),
                    savedBinaryContent.getFileName(),
                    contentType,
                    size
            );

            return toResponse(savedBinaryContent);

        } catch (RuntimeException e) {
            UUID binaryContentId =
                    savedBinaryContent == null
                            ? null
                            : savedBinaryContent.getId();

            log.error(
                    "파일 업로드 중 오류가 발생했습니다. binaryContentId={}, fileName={}, size={}",
                    binaryContentId,
                    request.getFileName(),
                    size,
                    e
            );

            // 파일 저장에 실패한 경우 이미 생성된 DB 메타데이터를 정리한다.
            if (savedBinaryContent != null
                    && savedBinaryContent.getId() != null) {

                try {
                    binaryContentRepository.deleteById(
                            savedBinaryContent.getId()
                    );

                    log.debug(
                            "파일 업로드 실패로 DB 메타데이터를 정리했습니다. binaryContentId={}",
                            savedBinaryContent.getId()
                    );

                } catch (RuntimeException cleanupException) {
                    log.error(
                            "파일 업로드 실패 후 DB 메타데이터 정리에도 실패했습니다. binaryContentId={}",
                            savedBinaryContent.getId(),
                            cleanupException
                    );

                    // 원래 업로드 실패 예외에 정리 실패 예외를 함께 기록한다.
                    e.addSuppressed(cleanupException);
                }
            }

            throw new BinaryContentStorageException(
                    binaryContentId,
                    "파일 업로드",
                    e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentResponse find(UUID id) {
        log.debug(
                "파일 메타데이터 조회를 시작합니다. binaryContentId={}",
                id
        );

        BinaryContent binaryContent = findBinaryContentById(id);

        log.debug(
                "파일 메타데이터 조회가 완료되었습니다. binaryContentId={}, fileName={}",
                binaryContent.getId(),
                binaryContent.getFileName()
        );

        return toResponse(binaryContent);
    }

    @Override
    @Transactional(readOnly = true)
    public BinaryContentDownloadResponse findForDownload(UUID id) {
        log.info(
                "파일 다운로드를 시작합니다. binaryContentId={}",
                id
        );

        BinaryContent binaryContent = findBinaryContentById(id);

        try {
            log.debug(
                    "파일 저장소에서 데이터를 조회합니다. binaryContentId={}, fileName={}",
                    binaryContent.getId(),
                    binaryContent.getFileName()
            );

            byte[] bytes =
                    binaryContentStorage.get(binaryContent.getId());

            log.info(
                    "파일 다운로드 데이터 조회가 완료되었습니다. binaryContentId={}, fileName={}, contentType={}, size={}",
                    binaryContent.getId(),
                    binaryContent.getFileName(),
                    safeContentType(binaryContent.getContentType()),
                    bytes == null ? 0 : bytes.length
            );

            return toDownloadResponse(binaryContent, bytes);

        } catch (RuntimeException e) {
            log.error(
                    "파일 다운로드 중 오류가 발생했습니다. binaryContentId={}, fileName={}",
                    binaryContent.getId(),
                    binaryContent.getFileName(),
                    e
            );

            throw new BinaryContentStorageException(
                    binaryContent.getId(),
                    "파일 다운로드",
                    e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        List<BinaryContentResponse> responses = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            log.debug("파일 메타데이터 목록 조회 요청이 비어 있습니다.");
            return responses;
        }

        log.debug(
                "파일 메타데이터 목록 조회를 시작합니다. requestCount={}",
                ids.size()
        );

        List<BinaryContent> binaryContents =
                binaryContentRepository.findAllByIdIn(ids);

        for (BinaryContent binaryContent : binaryContents) {
            responses.add(toResponse(binaryContent));
        }

        log.debug(
                "파일 메타데이터 목록 조회가 완료되었습니다. requestCount={}, resultCount={}",
                ids.size(),
                responses.size()
        );

        return responses;
    }

    @Override
    public void delete(UUID id) {
        log.info(
                "파일 삭제를 시작합니다. binaryContentId={}",
                id
        );

        if (id == null) {
            log.warn("파일 삭제에 실패했습니다. binaryContentId가 null입니다.");
            throw new IllegalArgumentException(
                    "삭제할 바이너리 콘텐츠 id는 null일 수 없습니다."
            );
        }

        BinaryContent binaryContent =
                binaryContentRepository.findById(id)
                        .orElseThrow(() -> {
                            log.warn(
                                    "삭제할 파일을 찾을 수 없습니다. binaryContentId={}",
                                    id
                            );

                            return new BinaryContentNotFoundException(id);
                        });

        try {
            log.debug(
                    "파일 삭제 요청을 처리합니다. binaryContentId={}, fileName={}",
                    binaryContent.getId(),
                    binaryContent.getFileName()
            );

            // DB 메타데이터를 삭제합니다.
            binaryContentRepository.delete(binaryContent);

            // 실제 저장소의 파일도 삭제합니다.
            binaryContentStorage.delete(id);

            log.info(
                    "파일 삭제가 완료되었습니다. binaryContentId={}, fileName={}",
                    binaryContent.getId(),
                    binaryContent.getFileName()
            );

        } catch (RuntimeException e) {
            log.error(
                    "파일 삭제 중 오류가 발생했습니다. binaryContentId={}, fileName={}",
                    binaryContent.getId(),
                    binaryContent.getFileName(),
                    e
            );

            throw new BinaryContentStorageException(
                    binaryContent.getId(),
                    "파일 삭제",
                    e
            );
        }
    }

    private BinaryContent findBinaryContentById(UUID id) {
        if (id == null) {
            log.warn("파일 조회에 실패했습니다. binaryContentId가 null입니다.");
            throw new IllegalArgumentException(
                    "조회할 바이너리 콘텐츠 id는 null일 수 없습니다."
            );
        }

        return binaryContentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "조회할 파일을 찾을 수 없습니다. binaryContentId={}",
                            id
                    );

                    return new BinaryContentNotFoundException(id);
                });
    }

    private void validate(BinaryContentCreateRequest request) {
        if (request.getFileName() == null
                || request.getFileName().isBlank()) {

            log.warn("파일 업로드 검증에 실패했습니다. 파일 이름이 비어 있습니다.");

            throw new IllegalArgumentException(
                    "파일 이름은 비어 있을 수 없습니다."
            );
        }

        if (request.getBytes() == null
                || request.getBytes().length == 0) {

            log.warn(
                    "파일 업로드 검증에 실패했습니다. 파일 데이터가 비어 있습니다. fileName={}",
                    request.getFileName()
            );

            throw new IllegalArgumentException(
                    "파일 데이터는 비어 있을 수 없습니다."
            );
        }
    }

    private BinaryContentResponse toResponse(
            BinaryContent binaryContent
    ) {
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