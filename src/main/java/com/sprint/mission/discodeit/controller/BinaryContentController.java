package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @PostMapping
    public BinaryContentDto create(
            @RequestPart("file") MultipartFile file
    ) {
        return binaryContentService.create(file);
    }

    @GetMapping("/{binaryContentId}")
    public BinaryContentDto find(
            @PathVariable UUID binaryContentId
    ) {
        return binaryContentService.find(binaryContentId);
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<?> download(
            @PathVariable UUID binaryContentId
    ) {
        return binaryContentService.download(binaryContentId);
    }
}