package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<BinaryContentResponse> find(
            @PathVariable UUID binaryContentId
    ) {
        return ResponseEntity.ok(binaryContentService.findById(binaryContentId));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<?>> findAllByIdIn(
            @RequestParam Collection<UUID> binaryContentIds
    ) {
        return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
    }
}
