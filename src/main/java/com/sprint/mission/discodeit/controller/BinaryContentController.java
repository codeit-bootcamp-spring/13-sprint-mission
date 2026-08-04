package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    /*
     * 기존 호환용 BinaryContent 생성
     *
     * API 명세 v1.2에는 직접 생성 API가 명시되어 있지는 않지만,
     * 기존 테스트/Postman 호환을 위해 유지한다.
     */
    @PostMapping({
            "/api/binaryContents",
            "/api/binary-contents"
    })
    public ResponseEntity<BinaryContentResponse> create(
            @Valid @RequestBody BinaryContentCreateRequest request
    ) {
        log.info(
                "BinaryContent 생성 요청을 받았습니다. fileName={}, contentType={}, size={}",
                request.getFileName(),
                request.getContentType(),
                request.getBytes().length
        );

        BinaryContentResponse response =
                binaryContentService.create(request);

        URI location =
                URI.create("/api/binaryContents/" + response.getId());

        log.info(
                "BinaryContent 생성 응답을 반환합니다. binaryContentId={}, status=201",
                response.getId()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /*
     * API 명세 v1.2 기준
     *
     * GET /api/binaryContents/{binaryContentId}
     *
     * 실제 파일 byte[]가 아니라 파일 메타데이터 JSON을 반환한다.
     */
    @GetMapping({
            "/api/binaryContents/{binaryContentId}",
            "/api/binary-contents/{binaryContentId}"
    })
    public ResponseEntity<BinaryContentResponse> find(
            @PathVariable UUID binaryContentId
    ) {
        log.debug(
                "BinaryContent 메타데이터 조회 요청을 받았습니다. binaryContentId={}",
                binaryContentId
        );

        BinaryContentResponse response =
                binaryContentService.find(binaryContentId);

        log.debug(
                "BinaryContent 메타데이터 조회 응답을 반환합니다. binaryContentId={}, fileName={}",
                response.getId(),
                response.getFileName()
        );

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     *
     * GET /api/binaryContents?binaryContentIds=uuid1&binaryContentIds=uuid2
     */
    @GetMapping({
            "/api/binaryContents",
            "/api/binary-contents"
    })
    public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
            @RequestParam List<UUID> binaryContentIds
    ) {
        log.debug(
                "BinaryContent 목록 조회 요청을 받았습니다. requestCount={}",
                binaryContentIds == null ? 0 : binaryContentIds.size()
        );

        List<BinaryContentResponse> responses =
                binaryContentService.findAllByIdIn(binaryContentIds);

        log.debug(
                "BinaryContent 목록 조회 응답을 반환합니다. resultCount={}",
                responses.size()
        );

        return ResponseEntity.ok(responses);
    }

    /*
     * API 명세 v1.2 기준
     *
     * GET /api/binaryContents/{binaryContentId}/download
     *
     * 실제 이미지/파일 byte[]를 반환한다.
     */
    @GetMapping({
            "/api/binaryContents/{binaryContentId}/download",
            "/api/binary-contents/{binaryContentId}/download"
    })
    public ResponseEntity<byte[]> download(
            @PathVariable UUID binaryContentId
    ) {
        log.info(
                "파일 다운로드 HTTP 요청을 받았습니다. binaryContentId={}",
                binaryContentId
        );

        BinaryContentDownloadResponse response =
                binaryContentService.findForDownload(binaryContentId);

        log.info(
                "파일 다운로드 응답을 반환합니다. binaryContentId={}, fileName={}, contentType={}, size={}",
                response.getId(),
                response.getFileName(),
                response.getContentType(),
                response.getSize()
        );

        return toFileResponse(response);
    }

    /*
     * 구버전 프론트 호환용
     *
     * GET /api/binaryContent/find?binaryContentId=...
     */
    @GetMapping("/api/binaryContent/find")
    public ResponseEntity<byte[]> findByRequestParam(
            @RequestParam UUID binaryContentId
    ) {
        log.info(
                "구버전 파일 다운로드 요청을 받았습니다. binaryContentId={}",
                binaryContentId
        );

        BinaryContentDownloadResponse response =
                binaryContentService.findForDownload(binaryContentId);

        log.info(
                "구버전 파일 다운로드 응답을 반환합니다. binaryContentId={}, fileName={}, size={}",
                response.getId(),
                response.getFileName(),
                response.getSize()
        );

        return toFileResponse(response);
    }

    /*
     * 기존 호환용 BinaryContent 삭제
     *
     * API 명세 v1.2에는 명시되어 있지는 않지만,
     * 기존 기능 호환을 위해 유지한다.
     */
    @DeleteMapping({
            "/api/binaryContents/{binaryContentId}",
            "/api/binary-contents/{binaryContentId}"
    })
    public ResponseEntity<Void> delete(
            @PathVariable UUID binaryContentId
    ) {
        log.info(
                "BinaryContent 삭제 HTTP 요청을 받았습니다. binaryContentId={}",
                binaryContentId
        );

        binaryContentService.delete(binaryContentId);

        log.info(
                "BinaryContent 삭제 응답을 반환합니다. binaryContentId={}, status=204",
                binaryContentId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    private ResponseEntity<byte[]> toFileResponse(
            BinaryContentDownloadResponse response
    ) {
        final String contentType =
                response.getContentType() == null
                        || response.getContentType().isBlank()
                        ? "application/octet-stream"
                        : response.getContentType();

        final String fileName =
                response.getFileName() == null
                        || response.getFileName().isBlank()
                        ? "download"
                        : response.getFileName();

        log.debug(
                "파일 응답을 구성합니다. binaryContentId={}, fileName={}, contentType={}, size={}",
                response.getId(),
                fileName,
                contentType,
                response.getSize()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(response.getSize())
                .headers(headers -> headers.setContentDisposition(
                        ContentDisposition.inline()
                                .filename(fileName, StandardCharsets.UTF_8)
                                .build()
                ))
                .body(response.getBytes());
    }
}