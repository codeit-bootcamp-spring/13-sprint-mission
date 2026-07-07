package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/binary-contents")
@RestController
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @GetMapping("/{binaryContentId}")
    public ResponseEntity<byte[]> find(@PathVariable UUID binaryContentId) {
        BinaryContentResponse response = binaryContentService.find(binaryContentId);

        byte[] imageBytes = Base64.getDecoder().decode(response.bytes());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .body(imageBytes);
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
}
