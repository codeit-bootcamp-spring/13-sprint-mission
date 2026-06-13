package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContent")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    //바이너리 파일을 1개 조회
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findBinaryContent(@RequestParam UUID binaryContentId) {
        BinaryContent binaryContent = binaryContentService.findBinaryContentById(binaryContentId);

        return ResponseEntity.ok().body(binaryContent);
    }

    //바이너리 파일을 여러 개 조회
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findBinaryContents(@RequestParam List<UUID> binaryContentIds) {
        List<BinaryContent> binaryContentList = binaryContentService.findAllBinaryContentByIdIn(binaryContentIds);

        return ResponseEntity.ok().body(binaryContentList);
    }


}
