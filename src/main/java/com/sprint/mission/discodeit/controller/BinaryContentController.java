package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
@Tag(name = "BinaryContent", description = "첨부 파일 API")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    //바이너리 파일을 1개 조회
    @Operation(summary = "첨부 파일 조회")
    @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공")
    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentDto> findBinaryContent(@Parameter(description = "조회할 첨부 파일 ID", required = true)
                                                              @PathVariable UUID binaryContentId) {
        BinaryContentDto binaryContent = binaryContentService.getBinaryContent(binaryContentId);

        return ResponseEntity.ok().body(binaryContent);
    }

    //바이너리 파일을 여러 개 조회
    @Operation(summary = "여러 첨부 파일 조회")
    @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContentDto>> findBinaryContents(@Parameter(description = "조회할 첨부 파일 ID 목록", required = true)
                                                                     @RequestParam List<UUID> binaryContentIds) {
        List<BinaryContentDto> binaryContentList = binaryContentService.getBinaryContentsByIdIn(binaryContentIds);

        return ResponseEntity.ok().body(binaryContentList);
    }

    //바이너리 파일을 다운로드
    @Operation(summary = "파일 다운로드")
    @ApiResponse(responseCode = "200", description = "파일 다운로드 성공")
    @RequestMapping(value = "/{binaryContentId}/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadBinaryContent(@Parameter(description = "다운로드할 파일 ID", required = true)
                                                          @PathVariable UUID binaryContentId) {
        log.debug("파일 다운로드 API 요청");

        return binaryContentStorage.download(binaryContentService.getBinaryContent(binaryContentId));
    }


}
