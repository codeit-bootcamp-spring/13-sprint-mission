package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
@RequestMapping("/api/readStatuses")
public interface ReadStatusApi {

  //생성
  @Operation(summary = "Message 읽음 상태 생성")
  @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨")
  @PostMapping("/{readStatusId}")
  ResponseEntity<ReadStatusDto> create(
      @RequestBody ReadStatusCreateRequest request
  );

  //수정
  @Operation(summary = "Message 읽음 상태 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Message 읽음 상태가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message 읽음 상태를 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "ReadStatus with id {readStatusId} not found")))
  })
  @PatchMapping("/{readStatusId}")
  ResponseEntity<ReadStatusDto> update(
      @Parameter(description = "수정할 읽음 상태") @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request
  );

  //조회
  @Operation(summary = "User의 Message 읽음 상태 목록 조회")
  @ApiResponse(responseCode = "200", description = "Message 읽음 상태 목록 조회 성공")
  @GetMapping
  ResponseEntity<List<ReadStatusDto>> findAllByUserId(
      @Parameter(description = "조회할 User ID") @RequestParam UUID userId
  );
}
