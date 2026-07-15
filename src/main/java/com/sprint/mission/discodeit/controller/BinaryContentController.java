package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
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
        BinaryContentResponse response =
                binaryContentService.find(binaryContentId);

        InputStream inputStream = binaryContentStorage.get(binaryContentId);
        Resource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .body(resource);
    }

    @GetMapping
    public List<BinaryContentResponse> finaAll(@RequestParam List<UUID> ids) {
        return binaryContentService.findAllByIdIn(ids);
    }

    @GetMapping("/find")
    public ResponseEntity<BinaryContentResponse> findByRequestParam(
            @RequestParam UUID binaryContentId
    ) {
        System.out.println("컨트롤러 들어옴 binaryContentId = " + binaryContentId);

        return ResponseEntity.ok(
                binaryContentService.find(binaryContentId)
        );
    }

    @GetMapping("/{binaryContentId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID binaryContentId) {
        BinaryContentResponse response = binaryContentService.find(binaryContentId);
        return binaryContentStorage.download(response);
    }

}
