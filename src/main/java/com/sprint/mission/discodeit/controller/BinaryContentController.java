package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "BinaryContent", description = "첨부 파일 API")
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @Operation(summary = "첨부 파일 단건 조회 API")
    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContent> findById(@PathVariable UUID binaryContentId) {
        BinaryContent content = binaryContentService.findEntity(binaryContentId);
        return ResponseEntity.status(HttpStatus.OK).body(content);
    }


    @Operation(summary = "여러 첨부 파일 조회 API")
    @GetMapping()
    public ResponseEntity<List<BinaryContentResponse>>findAllByUserId(@RequestParam List<UUID> binaryContentIds) {
        List<BinaryContentResponse> allByIdIn = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.status(HttpStatus.OK).body(allByIdIn);
    }
}
