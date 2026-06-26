package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> find(
            @PathVariable UUID binaryContentId
    ) {
        BinaryContent binaryContent = binaryContentService.findEntityById(binaryContentId);
        if (binaryContent == null) {
            throw new IllegalArgumentException("존재하지 않는 파일입니다.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(binaryContent.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + binaryContent.getFileName() + "\"")
                .body(binaryContent.getBytes());
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<?>> findAllByIdIn(
            @RequestParam Collection<UUID> ids
    ) {
        return ResponseEntity.ok(binaryContentService.findAllByIdIn(ids));
    }
}
