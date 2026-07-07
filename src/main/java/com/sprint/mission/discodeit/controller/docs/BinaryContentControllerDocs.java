package com.sprint.mission.discodeit.controller.docs;


import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "BinaryContent", description = "첨부 파일 API")
public interface BinaryContentControllerDocs {

    @Operation(summary = "첨부 파일 단건 조회 API")
    @GetMapping("/{binaryContentId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<BinaryContent> findById(@PathVariable UUID binaryContentId);


    @Operation(summary = "여러 첨부 파일 조회 API")
    @GetMapping()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<List<BinaryContentDto>>findAllByUserId(
            @RequestParam List<UUID> binaryContentIds);
}
