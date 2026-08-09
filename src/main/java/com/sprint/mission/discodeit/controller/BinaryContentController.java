package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    @GetMapping(value = "/{binaryContentId}")
    public ResponseEntity<BinaryContentDto> find(@PathVariable UUID binaryContentId) {
        return ResponseEntity.ok(binaryContentService.find(binaryContentId));
    }

    @GetMapping
    public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(@RequestParam List<UUID> binaryContentIds) {
        return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
    }

    @GetMapping(value = "/{binaryContentId}/download")
    public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
        log.info("파일 다운로드 요청: binaryContentId={}", binaryContentId);
        BinaryContentDto binaryContentDto = binaryContentService.find(binaryContentId);
        return binaryContentStorage.download(binaryContentDto);
    }
}
