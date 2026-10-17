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


@RequestMapping("/api/binaryContents")
@RestController
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContentDto> find(
            @PathVariable UUID binaryContentId
    ) {
        BinaryContentDto response =
                binaryContentService.find(binaryContentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BinaryContentDto>> findAll(
            @RequestParam List<UUID> ids
    ) {
        return ResponseEntity.ok(
                binaryContentService.findAllByIdIn(ids)
        );
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> download(
            @PathVariable UUID binaryContentId
    ) {
        BinaryContentDto dto =
                binaryContentService.find(binaryContentId);

        return binaryContentStorage.download(dto);
    }
}