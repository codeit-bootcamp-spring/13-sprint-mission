package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    // 바이너리 파일을 1개 또는 여러 개 조회
    @RequestMapping(value = "/{contentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentResponse> find(@PathVariable UUID contentId) {
        BinaryContentResponse foundContent = binaryContentService.find(contentId);
        return ResponseEntity.ok().body(foundContent);
    }
    @RequestMapping(value = "/{contentIds}", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(@PathVariable List<UUID> contentIds) {
        List<BinaryContentResponse> foundAllByIdIn = binaryContentService.findAllByIdIn(contentIds);
        return ResponseEntity.ok().body(foundAllByIdIn);
    }
}
