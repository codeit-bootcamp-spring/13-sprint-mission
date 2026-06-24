package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/binaryContents")
@RestController
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value =  "/{binaryContentId}", method = RequestMethod.GET)
    public BinaryContentResponse find(@PathVariable UUID binaryContentId) {
        return binaryContentService.find(binaryContentId);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContentResponse> finaAll(@RequestParam List<UUID> ids) {
        return binaryContentService.findAllByIdIn(ids);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentResponse> findByRequestParam(
            @RequestParam UUID binaryContentId
    ) {
        return ResponseEntity.ok(
                binaryContentService.find(binaryContentId)
        );
    }
}
