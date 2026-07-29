package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
@Slf4j
public class BinaryContentController {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContentResponse> getBinaryContentById(@PathVariable UUID binaryContentId) {
        BinaryContentResponse response = binaryContentService.find(binaryContentId)
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BinaryContentResponse>> getBinaryContents(
            @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {

        if (binaryContentIds != null && !binaryContentIds.isEmpty()) {
            return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> downloadBinaryContent(
            @PathVariable UUID binaryContentId
    ) {
        log.debug("파일 다운로드 API 요청: binaryContentId={}", binaryContentId);
        BinaryContentResponse metadata = binaryContentService.find(binaryContentId)
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

        log.info("파일 다운로드 응답 생성 완료: binaryContentId={}, fileName={}, size={}",
                metadata.id(), metadata.fileName(), metadata.size());
        return binaryContentStorage.download(metadata);
    }

}
