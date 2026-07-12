package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
@Tag(name = "ReadStatus", description = "Message 읽음 상태 API") // 그룹 묶기
public class ReadStatusController {

  private final ReadStatusService readStatusService;
  // 사용자가 채널 별 마지막으로 메시지 읽은 시간을 표현하는 도메인 모델
  // 사용자별 각 채널에 읽지 않은 메시지 확인하기 위해 활용

  // 특정 채널의 메시지 수신 정보 생성
  @Operation(summary = "Message 읽음 상태 생성")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음"),
      @ApiResponse(responseCode = "400", description = "이미 읽음 상태가 존재함")
  })
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<ReadStatusDto> create(
      @Valid @RequestBody ReadStatusCreateRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(readStatusService.create(request));
  }

  // 특정 채널의 메시지 수신 정보 수정
  @Operation(summary = "Message 읽음 상태 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Message 읽음 상태가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message 읽음 상태를 찾을 수 없음")
  })
  @RequestMapping(path = "/{readStatusId}", method = RequestMethod.PATCH)
  public ResponseEntity<ReadStatusDto> update(
      @Parameter(name = "readStatusId", in = ParameterIn.PATH, description = "수정할 읽음 상태 ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID readStatusId,
      @Valid @RequestBody ReadStatusUpdateRequest request) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(readStatusService.update(readStatusId, request));
  }

  // 특정 사용자의 메시지 수신 정보 조회
  @Operation(summary = "User의 Message 읽음 상태 목록 조회") // 엔드포인트 설명
  @ApiResponse(
      responseCode = "200",
      description = "Message 읽음 상태 목록 조회 성공",
      content = @Content(
          array = @ArraySchema(
              schema = @Schema(implementation = ReadStatusDto.class))))
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(
      @Parameter(name = "userId", in = ParameterIn.QUERY, description = "조회할 User ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @RequestParam UUID userId) {
    return ResponseEntity.ok().body(readStatusService.findAllByUserId(userId));
  }
}
