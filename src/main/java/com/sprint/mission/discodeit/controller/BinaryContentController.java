package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;


    //BinaryContent 단건 조회
    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContent> findById(@PathVariable UUID binaryContentId) {
        BinaryContent content = binaryContentService.findEntity(binaryContentId);
        return ResponseEntity.status(HttpStatus.OK).body(content);
    }



    //BinaryContent 조회 api
    @GetMapping()
    public ResponseEntity<List<BinaryContentResponse>>findAllByUserId(@RequestParam List<UUID> ids) {
        List<BinaryContentResponse> allByIdIn = binaryContentService.findAllByIdIn(ids);
        return ResponseEntity.status(HttpStatus.OK).body(allByIdIn);
    }
}
