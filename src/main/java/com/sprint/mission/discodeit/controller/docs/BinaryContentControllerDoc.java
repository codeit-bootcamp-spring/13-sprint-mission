package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface BinaryContentControllerDoc {

    @Operation(
            summary = "다수 조회",
            description = "id 에 해당하는 컨텐츠 다수 조회"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<List<BinaryContentDto>> findAll(
            @RequestParam List<UUID> binryContentIds
    );

    @Operation(
            summary = "단일 조회",
            description = "id 에 해당하는 컨텐츠 조회"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    ResponseEntity<BinaryContentDto> find(
            @PathVariable UUID binaryContentId
    );


    @Operation(
            summary = "파일 다운로드"
    )
    @ApiResponse(responseCode = "200",description = "파일 다운로드 성공")
    @GetMapping("/{binaryContentId}/download")
    ResponseEntity<?> download(@PathVariable UUID binaryContentId);


}
