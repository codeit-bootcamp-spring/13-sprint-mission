package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.storage.*;
import lombok.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RequestMapping("/api/binary-contents")
@RestController
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    @GetMapping("/{binaryContentId}")
    public ResponseEntity<Resource> find(@PathVariable UUID binaryContentId) {
        BinaryContentDto response =
                binaryContentService.find(binaryContentId);

        InputStream inputStream = binaryContentStorage.get(binaryContentId);
        Resource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<BinaryContentDto>> finaAll(@RequestParam List<UUID> ids) {
        List<BinaryContentDto> dto = binaryContentService.findAllByIdIn(ids);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/find")
    public ResponseEntity<BinaryContentDto> findByRequestParam(
            @RequestParam UUID binaryContentId
    ) {
        BinaryContentDto dto = binaryContentService.find(binaryContentId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
        BinaryContentDto dto = binaryContentService.find(binaryContentId);
        return binaryContentStorage.download(dto);
    }

}
