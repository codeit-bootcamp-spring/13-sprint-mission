package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
@Tag(name = "BinaryContent", description = "첨부 파일 API")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    //바이너리 파일을 1개 조회
    @Operation(summary = "첨부 파일 조회")
    @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공")
    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findBinaryContent(@Parameter(description = "조회할 첨부 파일 ID", required = true)
                                                           @PathVariable UUID binaryContentId) {
        BinaryContent binaryContent = binaryContentService.findBinaryContentById(binaryContentId);

        return ResponseEntity.ok().body(binaryContent);
    }

    //바이너리 파일을 여러 개 조회
    @Operation(summary = "여러 첨부 파일 조회")
    @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findBinaryContents(@Parameter(description = "조회할 첨부 파일 ID 목록", required = true)
                                                                  @RequestParam List<UUID> binaryContentIds) {
        List<BinaryContent> binaryContentList = binaryContentService.findAllBinaryContentByIdIn(binaryContentIds);

        return ResponseEntity.ok().body(binaryContentList);
    }


}
