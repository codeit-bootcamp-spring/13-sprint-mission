package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
@Tag(name = "BinaryContent", description = "첨부 파일 API") // 그룹 묶기
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  // 바이너리 파일을 1개 또는 여러 개 조회
  @Operation(summary = "첨부 파일 조회") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공"),
      @ApiResponse(responseCode = "404", description = "첨부 파일을 찾을 수 없음")})
  @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
  public ResponseEntity<BinaryContentResponse> find(
      @Parameter(name = "binaryContentId", in = ParameterIn.PATH, description = "조회할 첨부 파일 ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID binaryContentId) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(binaryContentService.find(binaryContentId));
  }

  @Operation(summary = "여러 첨부 파일 조회") // 엔드포인트 설명
  @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공")
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
      @Parameter(name = "binaryContentIds", in = ParameterIn.QUERY, description = "조회할 첨부 파일 ID 목록", required = true,
          array = @ArraySchema(schema = @Schema(type = "string", format = "uuid")))
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(binaryContentService.findAllByIdIn(binaryContentIds));
  }
}
